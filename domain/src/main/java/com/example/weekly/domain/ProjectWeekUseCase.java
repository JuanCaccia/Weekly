package com.example.weekly.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Orquestador de la proyección de semana.
 * Coordina el repositorio (puerto) con el motor de proyección (dominio).
 */
public class ProjectWeekUseCase {
    private final TaskRepository repository;
    private final SpleetEngine engine;
    private final CollisionDetector collisionDetector = new CollisionDetector();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    public ProjectWeekUseCase(TaskRepository repository, SpleetEngine engine) {
        this.repository = repository;
        this.engine = engine;
    }

    /**
     * Proyecta una semana basándose en una lista de SpleetTasks (Semana Ideal).
     * @param template Lista de plantillas base.
     * @param startMonday El lunes de la semana donde se quiere proyectar.
     * @return Objeto con las tareas guardadas y la información de las que saltaron por colisión.
     */
    public ProjectionResult executeSpleet(List<SpleetTask> template, LocalDate startMonday) {
        List<Task> potentialTasks = engine.projectSpleetToWeek(template, startMonday);
        
        List<Task> toSave = new ArrayList<>();
        List<String> skippedInfo = new ArrayList<>();

        for (Task projected : potentialTasks) {
            LocalDate date = projected.getDeadline().toLocalDate();
            // Obtener tareas ya existentes en esa fecha
            List<Task> existingTasks = repository.findByDate(date);
            
            // Obtener también las tareas que ya hemos decidido guardar en este mismo proceso
            List<Task> alreadyProcessedInThisBatch = toSave.stream()
                    .filter(t -> t.getDeadline().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            
            List<Task> allCheckTasks = new ArrayList<>(existingTasks);
            allCheckTasks.addAll(alreadyProcessedInThisBatch);

            List<TimeSlot> existingSlots = allCheckTasks.stream()
                    .filter(Task::isHasTimeBlock)
                    .map(t -> (TimeSlot) t)
                    .collect(Collectors.toList());

            if (projected.isHasTimeBlock()) {
                if (collisionDetector.hasCollision(projected.getStartTime(), projected.getEndTime(), existingSlots)) {
                    String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
                    String info = String.format("%s (%s %s - %s)", 
                            projected.getTitle(), 
                            dayName,
                            projected.getStartTime().format(TIME_FORMATTER),
                            projected.getEndTime().format(TIME_FORMATTER));
                    skippedInfo.add(info);
                    continue; // Saltar esta tarea si hay colisión
                }
            }
            toSave.add(projected);
        }

        List<Task> saved = persistProjectedTasks(toSave);
        return new ProjectionResult(saved, skippedInfo);
    }

    public static class ProjectionResult {
        public final List<Task> savedTasks;
        public final List<String> skippedTitles;

        public ProjectionResult(List<Task> savedTasks, List<String> skippedTitles) {
            this.savedTasks = savedTasks;
            this.skippedTitles = skippedTitles;
        }
    }

    /**
     * Elimina una lista de tareas (usado para la acción Deshacer).
     */
    public void undoProjection(List<Task> tasks) {
        if (tasks == null) return;
        for (Task task : tasks) {
            if (task.id != null) {
                repository.deleteById(task.id);
            }
        }
    }

    private List<Task> persistProjectedTasks(List<Task> tasks) {
        List<Task> savedTasks = new ArrayList<>();
        for (Task task : tasks) {
            savedTasks.add(repository.save(task));
        }
        return savedTasks;
    }
}
