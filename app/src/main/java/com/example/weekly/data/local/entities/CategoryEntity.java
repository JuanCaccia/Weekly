package com.example.weekly.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String name;
    public String colorHex;

    public CategoryEntity() {}

    public CategoryEntity(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }
}
