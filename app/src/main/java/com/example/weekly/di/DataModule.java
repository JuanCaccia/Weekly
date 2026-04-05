package com.example.weekly.di;

import android.content.Context;

import androidx.room.Room;

import com.example.weekly.data.local.AppDatabase;
import com.example.weekly.data.local.dao.EventDao;
import com.example.weekly.data.local.dao.SpleetHeaderDao;
import com.example.weekly.data.local.dao.SpleetTaskDao;
import com.example.weekly.data.local.dao.TaskDao;
import com.example.weekly.data.local.dao.TaskReminderDao;
import com.example.weekly.data.repositories.RoomEventRepository;
import com.example.weekly.data.repositories.RoomSpleetRepository;
import com.example.weekly.data.repositories.RoomTaskRepository;
import com.example.weekly.domain.EventRepository;
import com.example.weekly.domain.HeatmapService;
import com.example.weekly.domain.SpleetEngine;
import com.example.weekly.domain.SpleetRepository;
import com.example.weekly.domain.TaskRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataModule {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "weekly_db"
        )
        .fallbackToDestructiveMigration()
        .build();
    }

    @Provides
    public static TaskDao provideTaskDao(AppDatabase database) {
        return database.taskDao();
    }

    @Provides
    public static TaskReminderDao provideTaskReminderDao(AppDatabase database) {
        return database.taskReminderDao();
    }

    @Provides
    public static EventDao provideEventDao(AppDatabase database) {
        return database.eventDao();
    }

    @Provides
    public static SpleetTaskDao provideSpleetTaskDao(AppDatabase database) {
        return database.spleetTaskDao();
    }

    @Provides
    public static SpleetHeaderDao provideSpleetHeaderDao(AppDatabase database) {
        return database.spleetHeaderDao();
    }

    @Provides
    @Singleton
    public static TaskRepository provideTaskRepository(TaskDao taskDao, TaskReminderDao taskReminderDao) {
        return new RoomTaskRepository(taskDao, taskReminderDao);
    }

    @Provides
    @Singleton
    public static EventRepository provideEventRepository(EventDao eventDao) {
        return new RoomEventRepository(eventDao);
    }

    @Provides
    @Singleton
    public static SpleetRepository provideSpleetRepository(SpleetTaskDao spleetTaskDao, SpleetHeaderDao spleetHeaderDao) {
        return new RoomSpleetRepository(spleetTaskDao, spleetHeaderDao);
    }

    @Provides
    @Singleton
    public static HeatmapService provideHeatmapService() {
        return new HeatmapService();
    }

    @Provides
    @Singleton
    public static SpleetEngine provideSpleetEngine() {
        return new SpleetEngine();
    }
}
