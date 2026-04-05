package com.example.weekly.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskReminder;
import com.example.weekly.domain.TaskRepository;
import com.example.weekly.receivers.AlarmReceiver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TaskAlarmScheduler {

    private static final String TAG = "TaskAlarmScheduler";
    private final Context context;
    private final TaskRepository repository;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    public TaskAlarmScheduler(@ApplicationContext Context context, TaskRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    /**
     * Programa todas las alarmas para una tarea (basado en sus recordatorios).
     */
    public void scheduleTaskAlarms(Task task) {
        if (task.id == null || task.isCompletada()) return;

        Log.d(TAG, "Iniciando programación de alarmas para Tarea ID: " + task.id + " (" + task.titulo + ")");

        // Primero cancelamos alarmas previas para esta tarea
        cancelAlarms(task);

        SettingsManager settingsManager = new SettingsManager(context);
        if (!settingsManager.areGlobalNotificationsEnabled()) {
            Log.d(TAG, "Notificaciones globales desactivadas. Abortando programación para ID: " + task.id);
            return;
        }

        if (task.isImportant()) {
            if (!settingsManager.areImportantEventsEnabled()) return;
        } else {
            if (!settingsManager.areRemindersEnabled()) return;
        }

        LocalDateTime baseTime = null;
        if (task.isHasTimeBlock() && task.getStartTime() != null) {
            if (task.getDeadline() != null) {
                baseTime = LocalDateTime.of(task.getDeadline().toLocalDate(), task.getStartTime());
            }
        } else if (task.getDeadline() != null) {
            baseTime = task.getDeadline();
        }

        // Programar cada recordatorio
        List<TaskReminder> reminders = task.getReminders();
        if (reminders == null || reminders.isEmpty()) {
            // Si no hay recordatorios específicos, programar al momento del inicio
            if (baseTime != null && baseTime.isAfter(LocalDateTime.now())) {
                scheduleAlarm(task, baseTime, baseTime, 0, false);
            }
        } else {
            for (TaskReminder reminder : reminders) {
                if (reminder.minutesBefore != null) {
                    if (baseTime == null) continue;
                    LocalDateTime triggerTime = baseTime.minusMinutes(reminder.minutesBefore);
                    if (triggerTime.isAfter(LocalDateTime.now())) {
                        scheduleAlarm(task, triggerTime, baseTime, reminder.minutesBefore, true);
                    }
                } else if (reminder.specificTime != null) {
                    try {
                        LocalTime time = LocalTime.parse(reminder.specificTime);
                        LocalDate date = reminder.specificDate != null ? 
                                LocalDate.parse(reminder.specificDate) : 
                                (task.getDeadline() != null ? task.getDeadline().toLocalDate() : LocalDate.now());
                        
                        LocalDateTime triggerTime = LocalDateTime.of(date, time);
                        if (triggerTime.isAfter(LocalDateTime.now())) {
                            int identifier = getReminderIdentifier(reminder);
                            // Usamos el baseTime si existe, si no el triggerTime mismo (no debería pasar)
                            LocalDateTime actualBase = baseTime != null ? baseTime : triggerTime;
                            scheduleAlarm(task, triggerTime, actualBase, 10000 + identifier, true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error programando recordatorio específico", e);
                    }
                }
            }
            
            // PROGRAMAR TAMBIÉN LA NOTIFICACIÓN DE LA ACTIVIDAD EN SÍ
            if (baseTime != null && baseTime.isAfter(LocalDateTime.now())) {
                scheduleAlarm(task, baseTime, baseTime, 0, false);
            }
        }
    }

    private int getReminderIdentifier(TaskReminder reminder) {
        return Math.abs(Objects.hash(reminder.minutesBefore, reminder.specificTime, reminder.specificDate) % 10000);
    }

    private void scheduleAlarm(Task task, LocalDateTime triggerTime, LocalDateTime baseTime, int offset, boolean isPreReminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Intentamos un esquema de ID que reduzca colisiones:
        int notificationId = task.id.intValue();
        if (isPreReminder) {
            notificationId += 1000000 + offset;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TASK_ID", task.id);
        intent.putExtra("NOTIFICATION_ID", notificationId);
        intent.putExtra("TASK_TITLE", task.titulo);
        intent.putExtra("IS_IMPORTANT", task.isImportant());
        intent.putExtra("MINUTES_BEFORE", offset);
        intent.putExtra("IS_PRE_REMINDER", isPreReminder);
        
        // Datos extra para la notificación desplegable
        intent.putExtra("HAS_TIME_BLOCK", task.isHasTimeBlock());
        if (task.isHasTimeBlock() && task.getStartTime() != null) {
            intent.putExtra("START_TIME", task.getStartTime().format(timeFormatter));
        }
        
        // Pasamos el tiempo base para calcular "Faltan X dias..."
        long baseTimeMillis = baseTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        intent.putExtra("BASE_TIME_MILLIS", baseTimeMillis);

        if (task.isImportant()) {
            intent.putExtra("IMPORTANT_COLOR", task.getImportantColor());
        }

        // REQUISITO: Utilizar taskId como requestCode principal para evitar duplicados por ID de tarea
        int requestCode = notificationId;

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
        );

        long triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Log.d(TAG, String.format("Programando alarma [ID: %d][RequestCode: %d] para trigger: %d (%s)", 
                task.id, requestCode, triggerMillis, triggerTime.toString()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        }
    }

    public void cancelAlarms(Task task) {
        if (task.id == null) return;
        
        Log.d(TAG, "Cancelando todas las alarmas para Tarea ID: " + task.id);
        
        // REQUISITO: Cancelar siempre antes de programar para evitar duplicados
        
        List<TaskReminder> dbReminders = repository.findRemindersByTaskId(task.id);
        
        // Cancelar alarma base
        cancelSpecificAlarm(task, 0, false);

        // Cancelar recordatorios de la DB
        if (dbReminders != null) {
            for (TaskReminder reminder : dbReminders) {
                cancelReminderAlarm(task, reminder);
            }
        }

        // Cancelar los del objeto actual por si acaso
        if (task.getReminders() != null) {
            for (TaskReminder reminder : task.getReminders()) {
                cancelReminderAlarm(task, reminder);
            }
        }
    }

    private void cancelReminderAlarm(Task task, TaskReminder reminder) {
        if (reminder.minutesBefore != null) {
            cancelSpecificAlarm(task, reminder.minutesBefore, true);
        } else if (reminder.specificTime != null) {
            int identifier = getReminderIdentifier(reminder);
            cancelSpecificAlarm(task, 10000 + identifier, true);
        }
    }

    private void cancelSpecificAlarm(Task task, int offset, boolean isPreReminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int notificationId = task.id.intValue();
        if (isPreReminder) {
            notificationId += 1000000 + offset;
        }
        
        int requestCode = notificationId;

        Intent intent = new Intent(context, AlarmReceiver.class);
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
        );

        if (pendingIntent != null) {
            Log.d(TAG, "Cancelando alarma existente [ID: " + task.id + "][RequestCode: " + requestCode + "]");
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        } else {
            Log.d(TAG, "No existía alarma previa para [ID: " + task.id + "][RequestCode: " + requestCode + "]");
        }
    }

    public void cancelAllAlarms() {
        new Thread(() -> {
            Log.d(TAG, "Iniciando cancelación masiva de alarmas...");
            List<Task> allTasks = repository.findAll();
            for (Task task : allTasks) {
                cancelAlarms(task);
            }
            Log.d(TAG, "Todas las alarmas han sido canceladas.");
        }).start();
    }

    public void scheduleTaskAlarm(Task task) {
        // REQUISITO: Llamar siempre a cancelTaskAlarm con ese mismo ID antes de programar
        cancelAlarm(task);
        scheduleTaskAlarms(task);
    }
    
    public void cancelAlarm(Task task) {
        cancelAlarms(task);
    }
}
