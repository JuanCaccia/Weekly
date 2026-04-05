package com.example.weekly.data.mappers;

import com.example.weekly.data.local.entities.EventEntity;
import com.example.weekly.domain.Event;

import java.time.LocalDateTime;

public class EventMapper {
    public static Event toDomain(EventEntity entity) {
        if (entity == null) return null;
        Event event = new Event();
        event.id = entity.id;
        event.titulo = entity.title;
        event.fechaHoraInicio = entity.startDateTime != null ? LocalDateTime.parse(entity.startDateTime) : null;
        event.fechaHoraFin = entity.endDateTime != null ? LocalDateTime.parse(entity.endDateTime) : null;
        event.duracionMinutos = entity.durationMinutes;
        event.descripcion = entity.description;
        event.ubicacion = entity.location;
        event.esSemanaTipo = entity.isTemplate;
        event.dia = entity.dayOfWeek;
        event.semanaTipoId = entity.weekTemplateId;
        return event;
    }

    public static EventEntity toEntity(Event domain) {
        if (domain == null) return null;
        EventEntity entity = new EventEntity();
        entity.id = domain.id;
        entity.title = domain.titulo;
        entity.startDateTime = domain.fechaHoraInicio != null ? domain.fechaHoraInicio.toString() : null;
        entity.endDateTime = domain.fechaHoraFin != null ? domain.fechaHoraFin.toString() : null;
        entity.durationMinutes = domain.duracionMinutos;
        entity.description = domain.descripcion;
        entity.location = domain.ubicacion;
        entity.isTemplate = domain.esSemanaTipo;
        entity.dayOfWeek = domain.dia;
        entity.weekTemplateId = domain.semanaTipoId;
        return entity;
    }
}
