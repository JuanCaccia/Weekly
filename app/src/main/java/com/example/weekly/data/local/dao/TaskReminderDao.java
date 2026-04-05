package com.example.weekly.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weekly.data.local.entities.TaskReminderEntity;

import java.util.List;

@Dao
public interface TaskReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskReminderEntity reminder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TaskReminderEntity> reminders);

    @Query("SELECT * FROM task_reminders WHERE taskId = :taskId")
    List<TaskReminderEntity> findByTaskId(Long taskId);

    @Query("DELETE FROM task_reminders WHERE taskId = :taskId")
    void deleteByTaskId(Long taskId);

    @Delete
    void delete(TaskReminderEntity reminder);
}
