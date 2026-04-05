package com.example.weekly.domain;

import java.util.Objects;

public class TaskReminder {
    public Long id;
    public Long taskId;
    public Integer minutesBefore; // Minutos antes del inicio/deadline (para avisos relativos)
    
    // Para avisos en momentos específicos
    public Integer dayOfWeek; // 1-7 (Lunes-Domingo) para plantillas, o null
    public String specificTime; // "HH:mm"
    public String specificDate; // "yyyy-MM-dd" para tareas concretas

    public TaskReminder() {}

    public TaskReminder(Long id, Long taskId, Integer minutesBefore) {
        this.id = id;
        this.taskId = taskId;
        this.minutesBefore = minutesBefore;
    }

    public boolean isRelative() {
        return minutesBefore != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskReminder that = (TaskReminder) o;
        return Objects.equals(minutesBefore, that.minutesBefore) && 
               Objects.equals(id, that.id) && 
               Objects.equals(taskId, that.taskId) &&
               Objects.equals(dayOfWeek, that.dayOfWeek) &&
               Objects.equals(specificTime, that.specificTime) &&
               Objects.equals(specificDate, that.specificDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskId, minutesBefore, dayOfWeek, specificTime, specificDate);
    }
}
