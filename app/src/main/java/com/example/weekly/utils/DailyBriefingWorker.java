package com.example.weekly.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weekly.MainActivity;
import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class DailyBriefingWorker extends Worker {

    private final TaskRepository repository;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @AssistedInject
    public DailyBriefingWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            TaskRepository repository) {
        super(context, params);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        LocalDate today = LocalDate.now();
        List<Task> tasksToday = repository.findByDate(today);

        // 1. Filtrar tareas no completadas de hoy
        List<Task> pendingToday = tasksToday.stream()
                .filter(t -> !t.isCompletada())
                .collect(Collectors.toList());

        // 2. Buscar tareas "ignoradas" (pendientes de días anteriores)
        List<Task> allPending = repository.findPending();
        List<Task> ignoredTasks = allPending.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().isBefore(today))
                .collect(Collectors.toList());

        if (pendingToday.isEmpty() && ignoredTasks.isEmpty()) {
            return Result.success();
        }

        // 3. Resumen para la vista contraída
        long eventsToday = pendingToday.stream().filter(Task::isEvent).count();
        long tasksCountToday = pendingToday.size() - eventsToday;
        
        String summary;
        if (eventsToday > 0 && tasksCountToday > 0) {
            summary = String.format("Tienes %d tareas y %d eventos para hoy.", tasksCountToday, eventsToday);
        } else if (eventsToday > 0) {
            summary = String.format("Tienes %d eventos para hoy.", eventsToday);
        } else if (tasksCountToday > 0) {
            summary = String.format("Tienes %d tareas para hoy.", tasksCountToday);
        } else {
            summary = "No tienes actividades nuevas para hoy.";
        }

        // 4. Detalle para la vista expandida (más "linda")
        StringBuilder detail = new StringBuilder();
        
        if (!pendingToday.isEmpty()) {
            detail.append("📅 PARA HOY:\n");
            for (Task task : pendingToday) {
                detail.append(task.isEvent() ? "🔹 " : "🔸 ");
                if (task.isHasTimeBlock() && task.getStartTime() != null) {
                    detail.append("[").append(task.getStartTime().format(timeFormatter)).append("] ");
                }
                detail.append(task.getTitle()).append("\n");
            }
        }

        if (!ignoredTasks.isEmpty()) {
            if (detail.length() > 0) detail.append("\n");
            long ignoredEvents = ignoredTasks.stream().filter(Task::isEvent).count();
            long ignoredOnlyTasks = ignoredTasks.size() - ignoredEvents;
            
            detail.append("⚠️ PENDIENTES PASADOS:\n");
            detail.append("Ignoraste ");
            if (ignoredEvents > 0 && ignoredOnlyTasks > 0) {
                detail.append(ignoredEvents).append(" eventos y ").append(ignoredOnlyTasks).append(" tareas");
            } else if (ignoredEvents > 0) {
                detail.append(ignoredEvents).append(" eventos");
            } else {
                detail.append(ignoredOnlyTasks).append(" tareas");
            }
            detail.append(" de días anteriores.\n");
            
            // Mostrar los primeros 3 para no saturar
            ignoredTasks.stream().limit(3).forEach(t -> {
                detail.append(" • ").append(t.getTitle()).append("\n");
            });
            if (ignoredTasks.size() > 3) {
                detail.append(" ... y ").append(ignoredTasks.size() - 3).append(" más");
            }
        }

        showNotification(summary, detail.toString());

        return Result.success();
    }

    private void showNotification(String summary, String bigText) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("ACTION_SCROLL_TO_TODAY", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.CHANNEL_DAILY_BRIEFING)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Tu resumen diario 📝")
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Resumen de Actividades")
                        .bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(getApplicationContext().getColor(R.color.primary));

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(8000, builder.build());
        }
    }

    public static void schedule(Context context) {
        SettingsManager settingsManager = new SettingsManager(context);
        int hour = settingsManager.getBriefingHour();
        int minute = settingsManager.getBriefingMinute();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
        
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyBriefingWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_briefing",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
        );
    }
}
