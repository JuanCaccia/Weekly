package com.example.weekly;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import com.example.weekly.utils.NotificationHelper;
import com.example.weekly.utils.DailyBriefingWorker;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class WeeklyApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannels(this);
        DailyBriefingWorker.schedule(this);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
