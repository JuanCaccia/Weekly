package com.example.weekly.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.weekly.data.local.entities.TaskEntity;
import com.example.weekly.data.local.entities.TaskWithReminders;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(TaskEntity task);

    @Update
    void updateTask(TaskEntity task);

    @Query("SELECT * FROM tasks WHERE id = :id")
    TaskEntity findById(Long id);

    @Query("SELECT * FROM tasks")
    List<TaskEntity> findAll();

    @Transaction
    @Query("SELECT * FROM tasks WHERE isTemplate = 0 ORDER BY deadline ASC, startTime ASC")
    androidx.lifecycle.LiveData<List<TaskWithReminders>> getAllTasksLive();

    @Query("SELECT * FROM tasks WHERE isTemplate = 0 AND deadline LIKE :dateQuery")
    List<TaskEntity> getTasksByDay(String dateQuery);

    @Query("SELECT * FROM tasks WHERE isTemplate = 0 AND deadline BETWEEN :startDate AND :endDate")
    List<TaskEntity> getTasksBetweenDates(String startDate, String endDate);

    @Transaction
    @Query("SELECT * FROM tasks WHERE isTemplate = 0 AND deadline BETWEEN :startDate AND :endDate")
    androidx.lifecycle.LiveData<List<TaskWithReminders>> getTasksBetweenDatesLive(String startDate, String endDate);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isEvent = 0")
    List<TaskEntity> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE isTemplate = 1")
    List<TaskEntity> getTemplates();

    @Query("SELECT * FROM tasks WHERE isTemplate = 1 AND dayOfWeek = :weekTypeId")
    List<TaskEntity> findTemplatesByWeekType(Long weekTypeId);

    @Query("SELECT * FROM tasks WHERE isTemplate = 0 AND isCompleted = 0 AND isEvent = 0 " +
           "ORDER BY " +
           "CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, " +
           "deadline ASC")
    List<TaskEntity> findRemains();

    @Transaction
    @Query("SELECT * FROM tasks WHERE isTemplate = 0 AND isCompleted = 0 AND isEvent = 0 " +
           "ORDER BY " +
           "CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, " +
           "deadline ASC")
    androidx.lifecycle.LiveData<List<TaskWithReminders>> findRemainsLive();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND isTemplate = 0")
    List<TaskEntity> getArchivedTasks();

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND isTemplate = 0")
    androidx.lifecycle.LiveData<List<TaskWithReminders>> getArchivedTasksLive();

    /**
     * Elimina tareas que ya están completadas y tienen más de 30 días de antigüedad
     * o tareas vencidas (no completadas) con más de 30 días de antigüedad.
     */
    @Query("DELETE FROM tasks WHERE isTemplate = 0 AND (isCompleted = 1 OR deadline < :thresholdDate) AND deadline < :thresholdDate")
    void purgeOldTasks(String thresholdDate);

    @Delete
    void deleteTask(TaskEntity task);

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(Long id);

    @Query("DELETE FROM tasks")
    void deleteAll();
}
