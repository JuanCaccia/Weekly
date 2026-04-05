package com.example.weekly.data.local;

import androidx.room.TypeConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Converters {
    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String dateString) {
        return dateString == null ? null : LocalDateTime.parse(dateString);
    }

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String dateString) {
        return dateString == null ? null : LocalDate.parse(dateString);
    }

    @TypeConverter
    public static String fromLocalTime(LocalTime time) {
        return time == null ? null : time.toString();
    }

    @TypeConverter
    public static LocalTime toLocalTime(String timeString) {
        return timeString == null ? null : LocalTime.parse(timeString);
    }
}
