package com.example.weekly.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

public class NotificationHelper {

    public static final String CHANNEL_CRITICAL_EVENTS = "critical_events";
    public static final String CHANNEL_REMINDERS = "reminders";
    public static final String CHANNEL_DAILY_BRIEFING = "daily_briefing";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 1. Eventos Críticos: Importancia Máxima (Heads-up), Sonido distintivo
            NotificationChannel criticalChannel = new NotificationChannel(
                    CHANNEL_CRITICAL_EVENTS,
                    "Eventos Críticos",
                    NotificationManager.IMPORTANCE_HIGH
            );
            criticalChannel.setDescription("Notificaciones para eventos marcados como importantes.");
            criticalChannel.enableLights(true);
            criticalChannel.setLightColor(android.graphics.Color.RED);
            criticalChannel.enableVibration(true);
            
            // Sonido distintivo
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            criticalChannel.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttributes);

            // 2. Recordatorios: Importancia Alta
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Recordatorios",
                    NotificationManager.IMPORTANCE_HIGH // Subido a HIGH para asegurar que se vea bien
            );
            remindersChannel.setDescription("Notificaciones para tareas con horario de inicio.");

            // 3. Daily Briefing: Importancia Media/Alta para permitir expansión
            NotificationChannel briefingChannel = new NotificationChannel(
                    CHANNEL_DAILY_BRIEFING,
                    "Resumen Diario",
                    NotificationManager.IMPORTANCE_DEFAULT // Cambiado a DEFAULT para permitir expansión
            );
            briefingChannel.setDescription("Resumen de tus actividades del día.");

            manager.createNotificationChannel(criticalChannel);
            manager.createNotificationChannel(remindersChannel);
            manager.createNotificationChannel(briefingChannel);
        }
    }
}
