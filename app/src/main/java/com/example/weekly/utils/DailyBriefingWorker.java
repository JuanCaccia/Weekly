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
        LocalDate startRange = today.minusYears(1); // Ampliamos para capturar todos los pendientes antiguos
        LocalDate endRange = today.plusWeeks(2);    // 2 semanas adelante para "Próximamente"

        // Obtener actividades
        List<Task> allTasksInRange = repository.findTasksBetweenDates(startRange, endRange);

        // 1. Filtrar actividades de hoy
        List<Task> activitiesToday = allTasksInRange.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().equals(today))
                .filter(t -> !t.isCompletada())
                .collect(Collectors.toList());

        // 2. Tareas pendientes (solo TAREAS, no eventos, de días anteriores)
        List<Task> pendingTasks = allTasksInRange.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().isBefore(today))
                .filter(t -> !t.isCompletada() && !t.isEvent())
                .collect(Collectors.toList());

        // 3. Actividades futuras (mañana hasta 2 semanas, solo eventos o tareas importantes)
        List<Task> upcomingActivities = allTasksInRange.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().isAfter(today))
                .filter(t -> !t.isCompletada())
                .filter(t -> t.isEvent() || t.isImportant())
                .collect(Collectors.toList());

        if (activitiesToday.isEmpty() && pendingTasks.isEmpty() && upcomingActivities.isEmpty()) {
            return Result.success();
        }

        // 4. Resumen para la vista contraída
        long eventsToday = activitiesToday.stream().filter(Task::isEvent).count();
        long tasksCountToday = activitiesToday.size() - eventsToday;
        
        String summary;
        if (eventsToday > 0 && tasksCountToday > 0) {
            summary = String.format("Hoy: %d tareas y %d eventos", tasksCountToday, eventsToday);
        } else if (eventsToday > 0) {
            summary = String.format("Hoy: %d eventos", eventsToday);
        } else if (tasksCountToday > 0) {
            summary = String.format("Hoy: %d tareas", tasksCountToday);
        } else {
            summary = "No hay actividades para hoy, pero revisa tus pendientes";
        }

        // 5. Detalle para la vista expandida
        StringBuilder detail = new StringBuilder();
        
        if (!activitiesToday.isEmpty()) {
            detail.append("Para hoy:\n");
            for (Task task : activitiesToday) {
                detail.append("- ");
                if (task.isHasTimeBlock() && task.getStartTime() != null) {
                    detail.append("[").append(task.getStartTime().format(timeFormatter)).append("] ");
                }
                detail.append(task.getTitle()).append("\n");
            }
        }

        if (!pendingTasks.isEmpty()) {
            if (detail.length() > 0) detail.append("\n");
            detail.append("Tareas pendientes:\n");
            pendingTasks.stream().limit(3).forEach(t -> {
                detail.append(" • ").append(t.getTitle()).append("\n");
            });
            if (pendingTasks.size() > 3) {
                detail.append(" ... y ").append(pendingTasks.size() - 3).append(" más");
            }
        }

        if (!upcomingActivities.isEmpty()) {
            if (detail.length() > 0) detail.append("\n");
            detail.append("Próximamente:\n");
            upcomingActivities.stream().limit(3).forEach(t -> {
                String dateStr = t.getDeadline().format(DateTimeFormatter.ofPattern("dd/MM"));
                detail.append(" • ").append(t.getTitle()).append(" (").append(dateStr).append(")\n");
            });
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
                .setContentTitle("Tu resumen diario")
                .setContentText(summary)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Resumen de actividades")
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
