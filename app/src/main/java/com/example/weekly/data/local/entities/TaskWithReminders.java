package com.example.weekly.data.local.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TaskWithReminders {
    @Embedded
    public TaskEntity task;

    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    public List<TaskReminderEntity> reminders;
}
