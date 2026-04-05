package com.example.weekly.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SettingsManager {

    private static final String PREFS_NAME = "weekly_settings";
    private static final String KEY_GLOBAL_NOTIFICATIONS_ENABLED = "global_notifications_enabled";
    private static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    private static final String KEY_IMPORTANT_EVENTS_ENABLED = "important_events_enabled";
    private static final String KEY_BRIEFING_HOUR = "briefing_hour";
    private static final String KEY_BRIEFING_MINUTE = "briefing_minute";
    private static final String KEY_APP_THEME = "app_theme";

    private final SharedPreferences prefs;

    @Inject
    public SettingsManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean areGlobalNotificationsEnabled() {
        return prefs.getBoolean(KEY_GLOBAL_NOTIFICATIONS_ENABLED, true);
    }

    public void setGlobalNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_GLOBAL_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areRemindersEnabled() {
        return areGlobalNotificationsEnabled() && prefs.getBoolean(KEY_REMINDERS_ENABLED, true);
    }

    public void setRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply();
    }

    public boolean areImportantEventsEnabled() {
        return areGlobalNotificationsEnabled() && prefs.getBoolean(KEY_IMPORTANT_EVENTS_ENABLED, true);
    }

    public void setImportantEventsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_IMPORTANT_EVENTS_ENABLED, enabled).apply();
    }

    public int getBriefingHour() {
        return prefs.getInt(KEY_BRIEFING_HOUR, 8);
    }

    public int getBriefingMinute() {
        return prefs.getInt(KEY_BRIEFING_MINUTE, 0);
    }

    public void setBriefingTime(int hour, int minute) {
        prefs.edit()
                .putInt(KEY_BRIEFING_HOUR, hour)
                .putInt(KEY_BRIEFING_MINUTE, minute)
                .apply();
    }

    public int getAppTheme() {
        return prefs.getInt(KEY_APP_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setAppTheme(int themeMode) {
        prefs.edit().putInt(KEY_APP_THEME, themeMode).apply();
    }
}
