package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "task_reminders",
    foreignKeys = @ForeignKey(
        entity = TaskEntity.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("taskId")}
)
public class TaskReminderEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long taskId;
    public Integer minutesBefore;
    
    public Integer dayOfWeek;
    public String specificTime;
    public String specificDate;
}
