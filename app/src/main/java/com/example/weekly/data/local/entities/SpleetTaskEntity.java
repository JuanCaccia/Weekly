package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "spleet_tasks",
    foreignKeys = @ForeignKey(
        entity = SpleetHeaderEntity.class,
        parentColumns = "id",
        childColumns = "spleetHeaderId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("spleetHeaderId")}
)
public class SpleetTaskEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    public String title;
    public String priority;
    public boolean hasTimeBlock;
    public String startTime;
    public String endTime;
    public Integer dayOfWeek;
    
    public boolean isEvent;
    public boolean isImportant;
    
    public Long spleetHeaderId;
}
