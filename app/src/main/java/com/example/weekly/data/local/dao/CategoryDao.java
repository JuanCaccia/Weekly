package com.example.weekly.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weekly.data.local.entities.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CategoryEntity... categories);

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity findById(Long id);
}
