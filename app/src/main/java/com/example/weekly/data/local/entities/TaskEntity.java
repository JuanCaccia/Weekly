package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    public String title;
    public boolean isCompleted;
    public String priority; 
    
    public String deadline; 
    public boolean hasTimeBlock;
    public String startTime;
    public String endTime;
    
    public boolean isTemplate;
    public Integer dayOfWeek;
    public int reprogrammedCount;
    public boolean hasCollision;

    public boolean isEvent;
    public boolean isImportant;
    public String importantColor;
}
