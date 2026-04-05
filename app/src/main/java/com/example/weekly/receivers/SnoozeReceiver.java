package com.example.weekly.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("TASK_ID", -1);
        String title = intent.getStringExtra("TASK_TITLE");
        boolean isImportant = intent.getBooleanExtra("IS_IMPORTANT", false);
        String colorHex = intent.getStringExtra("IMPORTANT_COLOR");

        if (taskId == -1) return;

        // 1. Cancelar la notificación actual
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel((int) taskId);
        }

        // 2. Programar una nueva alarma para dentro de 15 minutos
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("TASK_ID", taskId);
        alarmIntent.putExtra("TASK_TITLE", title);
        alarmIntent.putExtra("IS_IMPORTANT", isImportant);
        if (colorHex != null) {
            alarmIntent.putExtra("IMPORTANT_COLOR", colorHex);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                alarmIntent,
                flags
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15);
        long triggerTime = calendar.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
