package com.example.weekly.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.weekly.data.local.dao.TaskDao;
import com.example.weekly.data.local.dao.TaskReminderDao;
import com.example.weekly.data.local.entities.TaskEntity;
import com.example.weekly.data.local.entities.TaskReminderEntity;
import com.example.weekly.data.mappers.TaskMapper;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskReminder;
import com.example.weekly.domain.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomTaskRepository implements TaskRepository {
    private final TaskDao taskDao;
    private final TaskReminderDao reminderDao;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public RoomTaskRepository(TaskDao taskDao, TaskReminderDao reminderDao) {
        this.taskDao = taskDao;
        this.reminderDao = reminderDao;
    }

    @Override
    public Task save(Task task) {
        // Lógica de Drag & Drop: Si pasa de no tener horario a tener uno, duración por defecto 60 min.
        if (task.getStartTime() != null && (task.getDuracionMinutos() == null || task.getDuracionMinutos() == 0)) {
            task.setDuracionMinutos(60);
            task.setHasTimeBlock(true);
            if (task.getEndTime() == null) {
                task.setEndTime(task.getStartTime().plusMinutes(60));
            }
        }

        TaskEntity entity = TaskMapper.toEntity(task);
        if (entity.id == null || entity.id == 0) {
            task.id = taskDao.insertTask(entity);
        } else {
            taskDao.updateTask(entity);
        }
        
        // Guardar recordatorios
        if (task.id != null) {
            saveReminders(task.id, task.getReminders());
            // Recargar recordatorios para tener los IDs correctos si es necesario
            task.setReminders(findRemindersByTaskId(task.id));
        }
        
        return task;
    }

    @Override
    public void deleteById(Long id) {
        taskDao.deleteById(id);
    }

    @Override
    public Optional<Task> findById(Long id) {
        TaskEntity entity = taskDao.findById(id);
        if (entity == null) return Optional.empty();
        
        List<TaskReminderEntity> reminders = reminderDao.findByTaskId(id);
        return Optional.of(TaskMapper.toDomain(entity, reminders));
    }

    @Override
    public List<Task> findAll() {
        return taskDao.findAll().stream()
                .map(entity -> {
                    List<TaskReminderEntity> reminders = reminderDao.findByTaskId(entity.id);
                    return TaskMapper.toDomain(entity, reminders);
                })
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<Task>> findAllLive() {
        return Transformations.map(taskDao.getAllTasksLive(), entities -> 
            entities.stream()
                .map(tr -> TaskMapper.toDomain(tr.task, tr.reminders))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Task> findByDate(LocalDate date) {
        String dateQuery = date.toString() + "%";
        return taskDao.getTasksByDay(dateQuery).stream()
                .map(entity -> {
                    List<TaskReminderEntity> reminders = reminderDao.findByTaskId(entity.id);
                    return TaskMapper.toDomain(entity, reminders);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTasksBetweenDates(LocalDate start, LocalDate end) {
        String startStr = start.atStartOfDay().format(DATE_TIME_FORMATTER);
        String endStr = end.atTime(23, 59, 59).format(DATE_TIME_FORMATTER);
        return taskDao.getTasksBetweenDates(startStr, endStr).stream()
                .map(entity -> {
                    List<TaskReminderEntity> reminders = reminderDao.findByTaskId(entity.id);
                    return TaskMapper.toDomain(entity, reminders);
                })
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<Task>> findTasksBetweenDatesLive(LocalDate start, LocalDate end) {
        String startStr = start.atStartOfDay().format(DATE_TIME_FORMATTER);
        String endStr = end.atTime(23, 59, 59).format(DATE_TIME_FORMATTER);
        return Transformations.map(taskDao.getTasksBetweenDatesLive(startStr, endStr), entities ->
            entities.stream()
                .map(tr -> TaskMapper.toDomain(tr.task, tr.reminders))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Task> findPending() {
        return taskDao.getPendingTasks().stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getTemplates() {
        return taskDao.getTemplates().stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTemplatesByWeekType(Long weekTypeId) {
        return taskDao.findTemplatesByWeekType(weekTypeId).stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<Task>> findRemainsLive() {
        return Transformations.map(taskDao.findRemainsLive(), entities ->
            entities.stream()
                .map(tr -> TaskMapper.toDomain(tr.task, tr.reminders))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Task> findRemains() {
        return taskDao.findRemains().stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<Task>> findArchivedLive() {
        return Transformations.map(taskDao.getArchivedTasksLive(), entities ->
                entities.stream()
                        .map(tr -> TaskMapper.toDomain(tr.task, tr.reminders))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<Task> findArchived() {
        return taskDao.getArchivedTasks().stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void saveReminders(Long taskId, List<TaskReminder> reminders) {
        reminderDao.deleteByTaskId(taskId);
        if (reminders != null && !reminders.isEmpty()) {
            List<TaskReminderEntity> entities = reminders.stream()
                    .map(r -> {
                        TaskReminderEntity entity = TaskMapper.toEntityReminder(r);
                        entity.id = null; // Forzar autogeneración ya que borramos los anteriores
                        entity.taskId = taskId;
                        return entity;
                    })
                    .collect(Collectors.toList());
            reminderDao.insertAll(entities);
        }
    }

    @Override
    public List<TaskReminder> findRemindersByTaskId(Long taskId) {
        return reminderDao.findByTaskId(taskId).stream()
                .map(TaskMapper::toDomainReminder)
                .collect(Collectors.toList());
    }

    @Override
    public void purgeOldData() {
        String thresholdDate = LocalDateTime.now().minusDays(30).format(DATE_TIME_FORMATTER);
        taskDao.purgeOldTasks(thresholdDate);
    }
}
