package com.example.weekly.ui.models;

import com.example.weekly.domain.HeatmapService;
import com.example.weekly.domain.Task;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DayPlan {
    public enum Type {
        NORMAL,
        REMAINS_HEADER
    }

    private final LocalDate date;
    private final List<Task> tasks; // Contiene tanto tareas como eventos mapeados
    private final Type type;
    private boolean isExpanded;
    private HeatmapService.DensityLevel densityLevel;

    public DayPlan(LocalDate date, List<Task> tasks) {
        this(date, tasks, Type.NORMAL);
    }

    public DayPlan(LocalDate date, List<Task> tasks, Type type) {
        this.date = date;
        this.tasks = tasks;
        this.type = type;
        this.isExpanded = false;
        this.densityLevel = HeatmapService.DensityLevel.NULA;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Type getType() {
        return type;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public HeatmapService.DensityLevel getDensityLevel() {
        return densityLevel != null ? densityLevel : HeatmapService.DensityLevel.NULA;
    }

    public void setDensityLevel(HeatmapService.DensityLevel densityLevel) {
        this.densityLevel = densityLevel;
    }

    public String getDayDisplayName() {
        String name = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayPlan dayPlan = (DayPlan) o;
        return isExpanded == dayPlan.isExpanded &&
                type == dayPlan.type &&
                Objects.equals(date, dayPlan.date) &&
                Objects.equals(tasks, dayPlan.tasks) &&
                densityLevel == dayPlan.densityLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, tasks, type, isExpanded, densityLevel);
    }
}
