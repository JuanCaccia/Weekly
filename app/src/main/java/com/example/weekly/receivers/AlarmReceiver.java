package com.example.weekly.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.weekly.MainActivity;
import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskRepository;
import com.example.weekly.utils.NotificationHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    public static final String ACTION_DONE = "com.example.weekly.ACTION_DONE";

    @Inject
    TaskRepository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("TASK_ID", -1);
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", (int) taskId);
        
        Log.d(TAG, String.format("Alarma disparada! [ID Tarea: %d][ID Notificación: %d] a las %d ms", 
                taskId, notificationId, System.currentTimeMillis()));

        if (ACTION_DONE.equals(intent.getAction())) {
            Log.d(TAG, "Acción 'Hecho' recibida para Tarea ID: " + taskId);
            markTaskAsDone(taskId);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(notificationId);
            }
            return;
        }

        String title = intent.getStringExtra("TASK_TITLE");
        boolean isImportant = intent.getBooleanExtra("IS_IMPORTANT", false);
        String colorHex = intent.getStringExtra("IMPORTANT_COLOR");
        int minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0);
        boolean isPreReminder = intent.getBooleanExtra("IS_PRE_REMINDER", false);
        long baseTimeMillis = intent.getLongExtra("BASE_TIME_MILLIS", 0);
        
        // Obtener datos de tiempo si tiene bloque horario
        boolean hasTimeBlock = intent.getBooleanExtra("HAS_TIME_BLOCK", false);
        String startTimeStr = intent.getStringExtra("START_TIME");

        Log.d(TAG, String.format("Mostrando notificación para: %s (Es recordatorio previo: %b)", title, isPreReminder));

        if (isImportant) {
            vibrateLong(context);
        }

        showNotification(context, taskId, notificationId, title, isImportant, colorHex, minutesBefore, isPreReminder, baseTimeMillis, hasTimeBlock, startTimeStr);
    }

    private void showNotification(Context context, long taskId, int notificationId, String title, boolean isImportant, String colorHex, int minutesBefore, boolean isPreReminder, long baseTimeMillis, boolean hasTimeBlock, String startTimeStr) {
        String channelId = isImportant ? NotificationHelper.CHANNEL_CRITICAL_EVENTS : NotificationHelper.CHANNEL_REMINDERS;

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.putExtra("ACTION_NAVIGATE_TO_TASK", true);
        contentIntent.putExtra("TASK_ID", taskId);
        contentIntent.putExtra("BASE_TIME_MILLIS", baseTimeMillis);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, notificationId, contentIntent, flags);

        // Acción Rápida: "Hecho"
        Intent doneIntent = new Intent(context, AlarmReceiver.class);
        doneIntent.setAction(ACTION_DONE);
        doneIntent.putExtra("TASK_ID", taskId);
        doneIntent.putExtra("NOTIFICATION_ID", notificationId);
        
        int doneFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            doneFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(context, notificationId + 1, doneIntent, doneFlags);

        // Acción Rápida: "Posponer"
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra("TASK_ID", taskId);
        snoozeIntent.putExtra("NOTIFICATION_ID", notificationId);
        snoozeIntent.putExtra("TASK_TITLE", title);
        snoozeIntent.putExtra("IS_IMPORTANT", isImportant);
        snoozeIntent.putExtra("IMPORTANT_COLOR", colorHex);
        
        int snoozeFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            snoozeFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 2, snoozeIntent, snoozeFlags);

        String contentTitle;
        if (isPreReminder && baseTimeMillis > 0) {
            contentTitle = getTimeRemainingString(baseTimeMillis);
        } else {
            contentTitle = isImportant ? "Evento Crítico" : "Recordatorio";
        }

        StringBuilder expandedText = new StringBuilder();
        expandedText.append(title != null ? title : "Tienes una tarea pendiente");
        if (hasTimeBlock && startTimeStr != null) {
            expandedText.append("\nHora: ").append(startTimeStr);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) 
                .setContentTitle(contentTitle)
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(expandedText.toString()))
                .setPriority(isImportant ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(!isImportant) 
                .setOngoing(isImportant)    
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_check, "Hecho", donePendingIntent)
                .addAction(R.drawable.ic_snooze, "Posponer 15 min", snoozePendingIntent);

        if (isImportant && colorHex != null && !colorHex.isEmpty()) {
            try {
                int color = Color.parseColor(colorHex);
                builder.setColor(color);
                builder.setColorized(true); 
            } catch (Exception e) {
                // Color inválido, ignorar
            }
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    private String getTimeRemainingString(long baseTimeMillis) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(baseTimeMillis), ZoneId.systemDefault());
        
        Duration duration = Duration.between(now, baseTime);
        if (duration.isNegative() || duration.isZero()) {
            return "¡Es ahora!";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder sb = new StringBuilder("Faltan ");
        boolean added = false;
        if (days > 0) {
            sb.append(days).append(days == 1 ? " día" : " días");
            added = true;
        }
        if (hours > 0) {
            if (added) sb.append(", ");
            sb.append(hours).append(hours == 1 ? " hora" : " horas");
            added = true;
        }
        if (minutes > 0 || !added) {
            if (added) sb.append(" y ");
            sb.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
        }
        
        return sb.toString();
    }

    private void vibrateLong(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    private void markTaskAsDone(long taskId) {
        new Thread(() -> {
            repository.findById(taskId).ifPresent(task -> {
                task.setCompletada(true);
                task.setFechaCompletada(LocalDateTime.now());
                repository.save(task);
                Log.d(TAG, "Tarea ID: " + taskId + " marcada como completada en DB");
            });
        }).start();
    }
}
