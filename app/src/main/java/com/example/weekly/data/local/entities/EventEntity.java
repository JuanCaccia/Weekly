package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String title;
    public String startDateTime;
    public String endDateTime;
    public Integer durationMinutes;
    public String description;
    public String location;
    public boolean isTemplate;
    public Integer dayOfWeek;
    public Long weekTemplateId;
}
