package com.example.weekly.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MilestoneService {

    /**
     * Devuelve una lista de fechas que tienen actividad (Tareas o Eventos).
     */
    public List<LocalDate> getDaysWithActivity(List<Task> tasks, List<Event> events, YearMonth month) {
        List<LocalDate> activityDates = new ArrayList<>();
        
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        // Extraer fechas de tareas en el rango
        List<LocalDate> taskDates = tasks.stream()
                .filter(t -> t.getDeadline() != null)
                .map(t -> t.getDeadline().toLocalDate())
                .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                .distinct()
                .collect(Collectors.toList());

        // Extraer fechas de eventos en el rango
        List<LocalDate> eventDates = events.stream()
                .map(Event::getDate)
                .filter(d -> !d.isBefore(start) && !d.isAfter(end))
                .distinct()
                .collect(Collectors.toList());

        activityDates.addAll(taskDates);
        for (LocalDate date : eventDates) {
            if (!activityDates.contains(date)) {
                activityDates.add(date);
            }
        }

        return activityDates.stream().sorted().collect(Collectors.toList());
    }
}
