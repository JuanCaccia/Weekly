package com.example.weekly.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weekly.data.local.entities.SpleetTaskEntity;

import java.util.List;

import androidx.lifecycle.LiveData;

@Dao
public interface SpleetTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SpleetTaskEntity task);

    @Update
    void update(SpleetTaskEntity task);

    @Delete
    void delete(SpleetTaskEntity task);

    @Query("SELECT * FROM spleet_tasks WHERE id = :id")
    SpleetTaskEntity findById(Long id);

    @Query("SELECT * FROM spleet_tasks ORDER BY dayOfWeek ASC, startTime ASC")
    List<SpleetTaskEntity> findAll();

    @Query("SELECT * FROM spleet_tasks ORDER BY dayOfWeek ASC, startTime ASC")
    LiveData<List<SpleetTaskEntity>> findAllLive();

    @Query("SELECT * FROM spleet_tasks WHERE dayOfWeek = :day ORDER BY startTime ASC")
    List<SpleetTaskEntity> findByDay(int day);

    @Query("SELECT * FROM spleet_tasks WHERE spleetHeaderId = :headerId ORDER BY dayOfWeek ASC, startTime ASC")
    List<SpleetTaskEntity> findByHeader(Long headerId);

    @Query("SELECT * FROM spleet_tasks WHERE spleetHeaderId = :headerId ORDER BY dayOfWeek ASC, startTime ASC")
    LiveData<List<SpleetTaskEntity>> findByHeaderLive(Long headerId);
}
