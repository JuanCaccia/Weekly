package com.example.weekly.domain;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa una tarea base o plantilla para el motor de proyección Spleet.
 * No tiene una fecha específica, sino un día de la semana (1-7).
 */
public class SpleetTask implements TimeSlot {
    public Long id;
    public String title;
    public Priority priority;
    public boolean hasTimeBlock;
    public LocalTime startTime;
    public LocalTime endTime;
    public Integer dayOfWeek; // 1 (Lunes) a 7 (Domingo)
    public Long spleetHeaderId;
    public boolean isEvent;
    public boolean isImportant;

    public SpleetTask() {}

    public SpleetTask(SpleetTask other) {
        if (other != null) {
            this.id = other.id;
            this.title = other.title;
            this.priority = other.priority;
            this.hasTimeBlock = other.hasTimeBlock;
            this.startTime = other.startTime;
            this.endTime = other.endTime;
            this.dayOfWeek = other.dayOfWeek;
            this.spleetHeaderId = other.spleetHeaderId;
            this.isEvent = other.isEvent;
            this.isImportant = other.isImportant;
        }
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpleetTask that = (SpleetTask) o;
        return hasTimeBlock == that.hasTimeBlock &&
                isEvent == that.isEvent &&
                isImportant == that.isImportant &&
                Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                priority == that.priority &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(dayOfWeek, that.dayOfWeek) &&
                Objects.equals(spleetHeaderId, that.spleetHeaderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, priority, hasTimeBlock, startTime, endTime, dayOfWeek, spleetHeaderId, isEvent, isImportant);
    }
}
