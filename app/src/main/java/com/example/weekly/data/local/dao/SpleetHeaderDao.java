package com.example.weekly.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weekly.data.local.entities.SpleetHeaderEntity;

import java.util.List;

@Dao
public interface SpleetHeaderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SpleetHeaderEntity header);

    @Update
    void update(SpleetHeaderEntity header);

    @Delete
    void delete(SpleetHeaderEntity header);

    @Query("SELECT * FROM spleet_headers WHERE id = :id")
    SpleetHeaderEntity findById(Long id);

    @Query("SELECT * FROM spleet_headers ORDER BY name ASC")
    List<SpleetHeaderEntity> findAll();

    @Query("SELECT * FROM spleet_headers ORDER BY name ASC")
    LiveData<List<SpleetHeaderEntity>> findAllLive();
}
