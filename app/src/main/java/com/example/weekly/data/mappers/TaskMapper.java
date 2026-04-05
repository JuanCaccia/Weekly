package com.example.weekly.data.mappers;

import com.example.weekly.data.local.entities.TaskEntity;
import com.example.weekly.data.local.entities.TaskReminderEntity;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskReminder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public static Task toDomain(TaskEntity entity) {
        if (entity == null) return null;
        
        Task task = new Task();
        task.id = entity.id;
        task.setTitle(entity.title);
        task.setCompletada(entity.isCompleted);
        
        if (entity.priority != null) {
            task.setPriority(com.example.weekly.domain.Priority.valueOf(entity.priority));
        }
        
        if (entity.deadline != null) {
            task.setDeadline(LocalDateTime.parse(entity.deadline, DATE_TIME_FORMATTER));
        }
        
        task.setHasTimeBlock(entity.hasTimeBlock);
        
        if (entity.startTime != null) {
            task.setStartTime(LocalTime.parse(entity.startTime, TIME_FORMATTER));
        }
        
        if (entity.endTime != null) {
            task.setEndTime(LocalTime.parse(entity.endTime, TIME_FORMATTER));
        }
        
        task.setTemplate(entity.isTemplate);
        task.setDayOfWeek(entity.dayOfWeek);
        task.reprogrammedCount = entity.reprogrammedCount;
        task.hasCollision = entity.hasCollision;
        
        task.setEvent(entity.isEvent);
        task.setImportant(entity.isImportant);
        task.setImportantColor(entity.importantColor);
        
        return task;
    }

    public static Task toDomain(TaskEntity entity, List<TaskReminderEntity> reminders) {
        Task task = toDomain(entity);
        if (task != null && reminders != null) {
            task.setReminders(reminders.stream()
                .map(TaskMapper::toDomainReminder)
                .collect(Collectors.toList()));
        }
        return task;
    }

    public static TaskReminder toDomainReminder(TaskReminderEntity entity) {
        if (entity == null) return null;
        TaskReminder domain = new TaskReminder();
        domain.id = entity.id;
        domain.taskId = entity.taskId;
        domain.minutesBefore = entity.minutesBefore;
        domain.dayOfWeek = entity.dayOfWeek;
        domain.specificTime = entity.specificTime;
        domain.specificDate = entity.specificDate;
        return domain;
    }

    public static TaskReminderEntity toEntityReminder(TaskReminder domain) {
        if (domain == null) return null;
        TaskReminderEntity entity = new TaskReminderEntity();
        entity.id = domain.id;
        entity.taskId = domain.taskId;
        entity.minutesBefore = domain.minutesBefore;
        entity.dayOfWeek = domain.dayOfWeek;
        entity.specificTime = domain.specificTime;
        entity.specificDate = domain.specificDate;
        return entity;
    }

    public static TaskEntity toEntity(Task task) {
        if (task == null) return null;

        TaskEntity entity = new TaskEntity();
        entity.id = task.id;
        entity.title = task.getTitle();
        entity.isCompleted = task.isCompletada();
        
        if (task.getPriority() != null) {
            entity.priority = task.getPriority().name();
        }
        
        if (task.getDeadline() != null) {
            entity.deadline = task.getDeadline().format(DATE_TIME_FORMATTER);
        }
        
        entity.hasTimeBlock = task.isHasTimeBlock();
        
        if (task.getStartTime() != null) {
            entity.startTime = task.getStartTime().format(TIME_FORMATTER);
        }
        
        if (task.getEndTime() != null) {
            entity.endTime = task.getEndTime().format(TIME_FORMATTER);
        }
        
        entity.isTemplate = task.isTemplate();
        entity.dayOfWeek = task.getDayOfWeek();
        entity.reprogrammedCount = task.reprogrammedCount;
        entity.hasCollision = task.hasCollision;
        
        entity.isEvent = task.isEvent();
        entity.isImportant = task.isImportant();
        entity.importantColor = task.getImportantColor();
        
        return entity;
    }
}
