package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spleet_headers")
public class SpleetHeaderEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    
    public String name;

    public SpleetHeaderEntity() {}

    public SpleetHeaderEntity(String name) {
        this.name = name;
    }
}
