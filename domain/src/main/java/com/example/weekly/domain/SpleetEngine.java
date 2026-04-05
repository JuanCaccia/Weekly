package com.example.weekly.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class SpleetEngine {

    @Inject
    public SpleetEngine() {
    }

    /**
     * Proyecta una lista de SpleetTask (plantillas) a una semana real.
     * @param template Lista de plantillas base.
     * @param targetMonday El lunes de la semana donde se quiere proyectar.
     * @return Lista de tareas reales listas para persistir.
     */
    public List<Task> projectSpleetToWeek(List<SpleetTask> template, LocalDate targetMonday) {
        List<Task> projectedTasks = new ArrayList<>();

        for (SpleetTask spleetTask : template) {
            Task task = new Task();
            task.titulo = spleetTask.title;
            task.prioridad = spleetTask.priority;
            task.tieneBloqueTiempo = spleetTask.hasTimeBlock;
            task.horaInicio = spleetTask.startTime;
            task.horaFin = spleetTask.endTime;
            task.dia = spleetTask.dayOfWeek;
            
            // Calcular fecha exacta: targetMonday + (dayOfWeek - 1)
            int daysToAdd = (spleetTask.dayOfWeek != null) ? (spleetTask.dayOfWeek - 1) : 0;
            LocalDate targetDate = targetMonday.plusDays(daysToAdd);
            
            // Asignar deadline (fecha + hora inicio o medianoche)
            if (task.tieneBloqueTiempo && task.horaInicio != null) {
                task.fechaHoraLimite = LocalDateTime.of(targetDate, task.horaInicio);
            } else {
                task.fechaHoraLimite = LocalDateTime.of(targetDate, LocalTime.MIDNIGHT);
            }

            // Estado inicial limpio
            task.completada = false;
            task.reprogrammedCount = 0;
            task.esSemanaTipo = false; 
            task.id = null; 
            
            projectedTasks.add(task);
        }
        
        return projectedTasks;
    }

    /**
     * Proyecta una semana tipo a la semana siguiente.
     */
    public List<Task> projectNextWeek(WeekTemplate template, LocalDate relativeToDate) {
        LocalDate nextMonday = relativeToDate.with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        List<Task> projectedTasks = new ArrayList<>();

        for (Task t : template.getTemplateTasks()) {
            Task projected = t.createProjectedInstance(nextMonday.plusDays(t.getDayOfWeek() - 1));
            projectedTasks.add(projected);
        }

        return projectedTasks;
    }
}
