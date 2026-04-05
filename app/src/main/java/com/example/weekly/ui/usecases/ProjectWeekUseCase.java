package com.example.weekly.ui.usecases;

import com.example.weekly.domain.CollisionDetector;
import com.example.weekly.domain.SpleetEngine;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskRepository;
import com.example.weekly.domain.TimeSlot;
import com.example.weekly.domain.WeekTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ProjectWeekUseCase {
    private final TaskRepository repository;
    private final SpleetEngine spleetEngine;
    private final CollisionDetector collisionDetector;

    @Inject
    public ProjectWeekUseCase(TaskRepository repository) {
        this.repository = repository;
        this.spleetEngine = new SpleetEngine();
        this.collisionDetector = new CollisionDetector();
    }

    public void execute(Long weekTypeId) {
        // 1. Obtener las tareas que son plantillas para este tipo de semana
        List<Task> templateTasks = repository.findTemplatesByWeekType(weekTypeId);
        
        // Creamos un objeto WeekTemplate temporal para el motor
        WeekTemplate template = new WeekTemplate(weekTypeId, "Semana Tipo");
        for (Task t : templateTasks) {
            template.addTaskToTemplate(t);
        }

        // 2. Proyectar usando el motor de dominio para la semana siguiente
        List<Task> projectedTasks = spleetEngine.projectNextWeek(template, LocalDate.now());

        // 3. Validar colisiones y guardar
        for (Task task : projectedTasks) {
            if (task.getDeadline() == null) continue;
            
            LocalDate targetDate = task.getDeadline().toLocalDate();
            List<Task> existingTasks = repository.findByDate(targetDate);
            
            boolean hasCollision = false;
            if (task.isHasTimeBlock()) {
                // Convertimos la lista de tareas a TimeSlot para el detector
                List<TimeSlot> existingSlots = new ArrayList<>(existingTasks);
                hasCollision = collisionDetector.hasCollision(
                        task.getStartTime(), 
                        task.getEndTime(), 
                        existingSlots
                );
            }

            if (!hasCollision) {
                repository.save(task);
            }
        }
    }
}
