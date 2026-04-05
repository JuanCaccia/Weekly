package com.example.weekly.data.mappers;

import com.example.weekly.data.local.entities.SpleetTaskEntity;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.SpleetTask;

import java.time.LocalTime;

public class SpleetTaskMapper {
    public static SpleetTask toDomain(SpleetTaskEntity entity) {
        if (entity == null) return null;
        SpleetTask task = new SpleetTask();
        task.id = entity.id;
        task.title = entity.title;
        task.priority = entity.priority != null ? Priority.valueOf(entity.priority) : null;
        task.hasTimeBlock = entity.hasTimeBlock;
        task.startTime = entity.startTime != null ? LocalTime.parse(entity.startTime) : null;
        task.endTime = entity.endTime != null ? LocalTime.parse(entity.endTime) : null;
        task.dayOfWeek = entity.dayOfWeek;
        task.spleetHeaderId = entity.spleetHeaderId;
        return task;
    }

    public static SpleetTaskEntity toEntity(SpleetTask domain) {
        if (domain == null) return null;
        SpleetTaskEntity entity = new SpleetTaskEntity();
        entity.id = domain.id;
        entity.title = domain.title;
        entity.priority = domain.priority != null ? domain.priority.name() : null;
        entity.hasTimeBlock = domain.hasTimeBlock;
        entity.startTime = domain.startTime != null ? domain.startTime.toString() : null;
        entity.endTime = domain.endTime != null ? domain.endTime.toString() : null;
        entity.dayOfWeek = domain.dayOfWeek;
        entity.spleetHeaderId = domain.spleetHeaderId;
        return entity;
    }
}
