package com.example.weekly.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.weekly.data.local.dao.EventDao;
import com.example.weekly.data.local.dao.TaskDao;
import com.example.weekly.data.local.dao.SpleetTaskDao;
import com.example.weekly.data.local.dao.SpleetHeaderDao;
import com.example.weekly.data.local.dao.TaskReminderDao;
import com.example.weekly.data.local.entities.EventEntity;
import com.example.weekly.data.local.entities.TaskEntity;
import com.example.weekly.data.local.entities.SpleetTaskEntity;
import com.example.weekly.data.local.entities.SpleetHeaderEntity;
import com.example.weekly.data.local.entities.TaskReminderEntity;

@Database(entities = {TaskEntity.class, EventEntity.class, SpleetTaskEntity.class, SpleetHeaderEntity.class, TaskReminderEntity.class}, version = 16, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract EventDao eventDao();
    public abstract SpleetTaskDao spleetTaskDao();
    public abstract SpleetHeaderDao spleetHeaderDao();
    public abstract TaskReminderDao taskReminderDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "weekly_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
