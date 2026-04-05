package com.example.weekly.domain;

import java.util.List;

public class HeatmapService {
    public enum DensityLevel { NULA, BAJA, MEDIA, ALTA, MUY_ALTA }

    public static class DensityResult {
        public final double score;
        public final DensityLevel level;

        public DensityResult(double score, DensityLevel level) {
            this.score = score;
            this.level = level;
        }
    }

    public DensityResult calculateCompletedDensity(List<Task> tasks) {
        double score = 0.0;
        int totalTasks = tasks.size();
        if (totalTasks == 0) return new DensityResult(0, DensityLevel.NULA);

        long completedCount = tasks.stream().filter(Task::isCompletada).count();
        double percentage = (double) completedCount / totalTasks;

        // Clasificación por porcentaje de éxito (0.0 a 1.0)
        DensityLevel level;
        if (percentage <= 0.0) level = DensityLevel.NULA;
        else if (percentage <= 0.25) level = DensityLevel.BAJA;
        else if (percentage <= 0.50) level = DensityLevel.MEDIA;
        else if (percentage <= 0.75) level = DensityLevel.ALTA;
        else level = DensityLevel.MUY_ALTA;

        return new DensityResult(percentage, level);
    }

    public DensityResult calculateDensity(List<Object> activitiesForDay) {
        double score = 0.0;
        for (Object a : activitiesForDay) {
            double base = 0.0;
            if (a instanceof Task) {
                Task t = (Task) a;
                base = t.isHasTimeBlock() ? 2.0 : 1.0;
                if (t.getPriority() == Priority.HIGH) {
                    base *= 1.5;
                }
            } else if (a instanceof Event) {
                base = 3.0;
            }
            score += base;
        }

        DensityLevel level = classify(score);
        return new DensityResult(score, level);
    }

    private DensityLevel classify(double score) {
        if (score <= 0.0) return DensityLevel.NULA;
        if (score <= 5.0) return DensityLevel.BAJA;
        if (score <= 10.0) return DensityLevel.MEDIA;
        return DensityLevel.ALTA;
    }
}
