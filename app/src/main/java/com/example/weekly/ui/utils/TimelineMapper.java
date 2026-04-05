package com.example.weekly.ui.utils;

import android.content.Context;
import android.util.TypedValue;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimelineMapper {
    public static final int HOUR_HEIGHT_DP = 100;

    public static int getTopMarginPx(LocalTime time, Context context) {
        return getTopMarginPx(time, 0, context);
    }

    public static int getTopMarginPx(LocalTime time, int startHour, Context context) {
        if (time == null) return 0;
        float minutesSinceStart = (time.getHour() - startHour) * 60 + time.getMinute();
        float dpValue = (minutesSinceStart / 60f) * HOUR_HEIGHT_DP;
        return dpToPx(dpValue, context);
    }

    public static int getHeightPx(LocalTime start, LocalTime end, Context context) {
        if (start == null || end == null) return dpToPx(60, context);
        long durationMinutes = ChronoUnit.MINUTES.between(start, end);
        float dpValue = (durationMinutes / 60f) * HOUR_HEIGHT_DP;
        return dpToPx(dpValue, context);
    }

    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}
