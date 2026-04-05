package com.example.weekly.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskRepository;
import com.example.weekly.utils.TaskAlarmScheduler;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Inject
    TaskRepository repository;

    @Inject
    TaskAlarmScheduler alarmScheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            
            Log.d(TAG, "Reinicio detectado. Reprogramando todas las alarmas futuras...");
            reprogramAlarms();
        }
    }

    private void reprogramAlarms() {
        new Thread(() -> {
            try {
                // Obtenemos todas las tareas
                List<Task> allTasks = repository.findAll();
                LocalDateTime now = LocalDateTime.now();

                int count = 0;
                for (Task task : allTasks) {
                    // Solo reprogramamos si:
                    // 1. No está completada
                    // 2. Tiene un deadline
                    // 3. El trigger de la alarma es posterior a 'ahora'
                    if (!task.isCompletada() && task.getDeadline() != null) {
                        
                        LocalDateTime triggerTime;
                        if (task.isHasTimeBlock() && task.getStartTime() != null) {
                            triggerTime = LocalDateTime.of(task.getDeadline().toLocalDate(), task.getStartTime());
                        } else {
                            triggerTime = task.getDeadline();
                        }

                        if (triggerTime.isAfter(now)) {
                            alarmScheduler.scheduleTaskAlarm(task);
                            count++;
                        }
                    }
                }
                Log.d(TAG, "Se han reprogramado " + count + " alarmas futuras.");
            } catch (Exception e) {
                Log.e(TAG, "Error al reprogramar alarmas tras el reinicio", e);
            }
        }).start();
    }
}
