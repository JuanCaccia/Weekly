package com.example.weekly.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Event implements TimeSlot {
    public Long id;
    public String titulo;
    public LocalDateTime fechaHoraInicio; // nullable
    public LocalDateTime fechaHoraFin; // nullable
    public Integer duracionMinutos; // nullable (fin - inicio)
    public String descripcion;
    public String ubicacion; // nullable
    public boolean esSemanaTipo;
    public Integer dia; // 1..7 nullable
    public Long semanaTipoId; // FK nullable
    public final boolean isEvent = true;

    public Event() {}

    public Event(Long id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }

    public LocalDate getDate() {
        return fechaHoraInicio != null ? fechaHoraInicio.toLocalDate() : null;
    }

    @Override
    public LocalTime getStartTime() {
        return fechaHoraInicio != null ? fechaHoraInicio.toLocalTime() : null;
    }

    @Override
    public LocalTime getEndTime() {
        return fechaHoraFin != null ? fechaHoraFin.toLocalTime() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return esSemanaTipo == event.esSemanaTipo &&
                Objects.equals(id, event.id) &&
                Objects.equals(titulo, event.titulo) &&
                Objects.equals(fechaHoraInicio, event.fechaHoraInicio) &&
                Objects.equals(fechaHoraFin, event.fechaHoraFin) &&
                Objects.equals(duracionMinutos, event.duracionMinutos) &&
                Objects.equals(descripcion, event.descripcion) &&
                Objects.equals(ubicacion, event.ubicacion) &&
                Objects.equals(dia, event.dia) &&
                Objects.equals(semanaTipoId, event.semanaTipoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, fechaHoraInicio, fechaHoraFin, duracionMinutos, descripcion, ubicacion, esSemanaTipo, dia, semanaTipoId);
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + id + ", titulo='" + titulo + '\'' + '}';
    }
}
