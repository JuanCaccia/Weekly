package com.example.weekly.domain;

import java.time.LocalTime;
import java.util.List;

public class CollisionDetector {

    /**
     * Verifica si una nueva tarea con bloque de tiempo colisiona con 
     * eventos o tareas existentes en el mismo día.
     */
    public boolean hasCollision(LocalTime newStart, LocalTime newEnd, List<TimeSlot> existingSlots) {
        if (newStart == null || newEnd == null) return false;
        
        for (TimeSlot slot : existingSlots) {
            LocalTime slotStart = slot.getStartTime();
            LocalTime slotEnd = slot.getEndTime();
            
            // Solo verificamos colisión si ambos tienen bloque de tiempo
            if (slotStart != null && slotEnd != null) {
                // Regla de traslape: (StartA < EndB) AND (EndA > StartB)
                if (newStart.isBefore(slotEnd) && newEnd.isAfter(slotStart)) {
                    return true; // Hay colisión
                }
            }
        }
        return false;
    }

    /**
     * Valida la integridad de una tarea según las reglas de Weekly.
     */
    public void validateTask(Task task) throws IllegalArgumentException {
        if (task.isHasTimeBlock()) {
            if (task.getStartTime() == null || task.getEndTime() == null) {
                throw new IllegalArgumentException("Las tareas con bloque de tiempo deben tener hora de inicio y fin.");
            }
            if (!task.getStartTime().isBefore(task.getEndTime())) {
                throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de fin.");
            }
        }
    }
}
