package com.example.weekly.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model Task (clean domain, no persistence annotations).
 */
public class Task implements TimeSlot {
    public Long id;
    public String titulo;               
    public boolean completada;
    public LocalDateTime fechaCompletada;
    public Priority prioridad;
    public LocalDateTime fechaHoraLimite;
    public boolean tieneBloqueTiempo;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public Integer duracionMinutos;
    public boolean esSemanaTipo;
    public Integer dia;
    public Long semanaTipoId;
    public Long repeticionId;
    public int reprogrammedCount;
    public boolean hasCollision; // Flag visual para colisiones en proyección
    
    public boolean isEvent;
    public boolean isImportant;
    public String importantColor;

    private List<TaskReminder> reminders = new ArrayList<>();

    public Task() {}

    public Long getId() { return id; }

    public String getTitle() { return titulo; }
    public void setTitle(String title) { this.titulo = title; }

    public Priority getPriority() { return prioridad; }
    public void setPriority(Priority p) { this.prioridad = p; }

    public boolean isHasTimeBlock() { return tieneBloqueTiempo; }
    public void setHasTimeBlock(boolean hasTimeBlock) { this.tieneBloqueTiempo = hasTimeBlock; }

    public LocalTime getStartTime() { return horaInicio; }
    public void setStartTime(LocalTime startTime) { this.horaInicio = startTime; }

    public LocalTime getEndTime() { return horaFin; }
    public void setEndTime(LocalTime endTime) { this.horaFin = endTime; }

    public LocalDateTime getDeadline() { return fechaHoraLimite; }
    public void setDeadline(LocalDateTime deadline) { this.fechaHoraLimite = deadline; }

    public boolean isTemplate() { return esSemanaTipo; }
    public void setTemplate(boolean template) { this.esSemanaTipo = template; }

    public Integer getDayOfWeek() { return dia; }
    public void setDayOfWeek(Integer day) { this.dia = day; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completed) { this.completada = completed; }

    public LocalDateTime getFechaCompletada() { return fechaCompletada; }
    public void setFechaCompletada(LocalDateTime fechaCompletada) { this.fechaCompletada = fechaCompletada; }
    
    public LocalDateTime getArchivedAt() { return fechaCompletada; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.fechaCompletada = archivedAt; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public Long getSemanaTipoId() { return semanaTipoId; }
    public void setSemanaTipoId(Long semanaTipoId) { this.semanaTipoId = semanaTipoId; }

    public Long getRepeticionId() { return repeticionId; }
    public void setRepeticionId(Long repeticionId) { this.repeticionId = repeticionId; }

    public boolean isImportant() {
        return isImportant && isEvent;
    }

    public void setImportant(boolean important) {
        if (important && !isEvent) {
            this.isImportant = false;
        } else {
            this.isImportant = important;
        }
    }

    public String getImportantColor() {
        return importantColor;
    }

    public void setImportantColor(String importantColor) {
        this.importantColor = importantColor;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
        if (!isEvent) {
            isImportant = false;
        }
    }

    public List<TaskReminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<TaskReminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return completada == task.completada &&
                reprogrammedCount == task.reprogrammedCount &&
                hasCollision == task.hasCollision &&
                isEvent == task.isEvent &&
                isImportant == task.isImportant &&
                Objects.equals(id, task.id) &&
                Objects.equals(titulo, task.titulo) &&
                prioridad == task.prioridad &&
                Objects.equals(horaInicio, task.horaInicio) &&
                Objects.equals(horaFin, task.horaFin) &&
                Objects.equals(importantColor, task.importantColor) &&
                Objects.equals(reminders, task.reminders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, completada, prioridad, horaInicio, horaFin, reprogrammedCount, hasCollision, isEvent, isImportant, importantColor, reminders);
    }
    
    public Task copy() {
        Task task = new Task();
        task.id = this.id;
        task.titulo = this.titulo;
        task.completada = this.completada;
        task.fechaCompletada = this.fechaCompletada;
        task.prioridad = this.prioridad;
        task.fechaHoraLimite = this.fechaHoraLimite;
        task.tieneBloqueTiempo = this.tieneBloqueTiempo;
        task.horaInicio = this.horaInicio;
        task.horaFin = this.horaFin;
        task.duracionMinutos = this.duracionMinutos;
        task.esSemanaTipo = this.esSemanaTipo;
        task.dia = this.dia;
        task.semanaTipoId = this.semanaTipoId;
        task.repeticionId = this.repeticionId;
        task.reprogrammedCount = this.reprogrammedCount;
        task.hasCollision = this.hasCollision;
        task.isEvent = this.isEvent;
        task.isImportant = this.isImportant;
        task.importantColor = this.importantColor;
        task.reminders = new ArrayList<>(this.reminders);
        return task;
    }

    public Task createProjectedInstance(LocalDate targetDate) {
        Task projected = new Task();
        projected.setTitle(this.titulo);
        projected.setPriority(this.prioridad);
        projected.setHasTimeBlock(this.tieneBloqueTiempo);
        projected.setStartTime(this.horaInicio);
        projected.setEndTime(this.horaFin);
        projected.setDuracionMinutos(this.duracionMinutos);
        
        if (this.fechaHoraLimite != null) {
            projected.setDeadline(LocalDateTime.of(targetDate, this.fechaHoraLimite.toLocalTime()));
        } else {
            projected.setDeadline(LocalDateTime.of(targetDate, LocalTime.MIDNIGHT));
        }

        projected.setTemplate(false);
        projected.setCompletada(false);
        projected.reprogrammedCount = 0;
        projected.id = null;
        projected.hasCollision = false;
        
        projected.isEvent = this.isEvent;
        projected.isImportant = this.isImportant;
        projected.importantColor = this.importantColor;
        projected.reminders = new ArrayList<>(this.reminders);
        
        return projected;
    }
}
