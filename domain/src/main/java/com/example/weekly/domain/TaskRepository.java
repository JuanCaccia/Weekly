package com.example.weekly.domain;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task persistence. Hexagonal port — no persistence details here.
 */
public interface TaskRepository {
    Task save(Task task);
    void deleteById(Long id);
    Optional<Task> findById(Long id);
    List<Task> findAll();
    
    // Contrato Reactivo (Puerto)
    LiveData<List<Task>> findAllLive();
    
    List<Task> findByDate(LocalDate date);
    List<Task> findTasksBetweenDates(LocalDate start, LocalDate end);
    LiveData<List<Task>> findTasksBetweenDatesLive(LocalDate start, LocalDate end);

    List<Task> findPending();
    List<Task> getTemplates();
    List<Task> findTemplatesByWeekType(Long weekTypeId);

    // Remains
    LiveData<List<Task>> findRemainsLive();
    List<Task> findRemains();

    // Archived
    LiveData<List<Task>> findArchivedLive();
    List<Task> findArchived();

    // Reminders
    void saveReminders(Long taskId, List<TaskReminder> reminders);
    List<TaskReminder> findRemindersByTaskId(Long taskId);

    /**
     * Elimina tareas completadas o vencidas con más de 30 días de antigüedad.
     */
    void purgeOldData();
}
