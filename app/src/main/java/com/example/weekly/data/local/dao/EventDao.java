package com.example.weekly.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weekly.data.local.entities.EventEntity;

import java.util.List;

import androidx.lifecycle.LiveData;

@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(EventEntity event);

    @Query("SELECT * FROM events WHERE id = :id")
    EventEntity findById(Long id);

    @Query("SELECT * FROM events WHERE isTemplate = 0")
    LiveData<List<EventEntity>> getAllEventsLive();

    @Query("SELECT * FROM events WHERE isTemplate = 0 AND startDateTime LIKE :dateQuery")
    List<EventEntity> getEventsByDay(String dateQuery);

    @Delete
    void deleteEvent(EventEntity event);

    @Query("DELETE FROM events WHERE id = :id")
    void deleteById(Long id);

    @Query("DELETE FROM events")
    void deleteAll();
}
