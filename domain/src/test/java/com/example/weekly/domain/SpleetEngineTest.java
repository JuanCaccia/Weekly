package com.example.weekly.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SpleetEngineTest {

    private SpleetEngine engine;

    @Before
    public void setUp() {
        engine = new SpleetEngine();
    }

    @Test
    public void testProjectNextWeekFromTemplate() {
        // GIVEN: Una plantilla con una tarea el lunes (día 1) y otra el miércoles (día 3)
        WeekTemplate template = new WeekTemplate(1L, "Semana de Prueba");
        
        Task taskLunes = new Task();
        taskLunes.setTitle("Gym Lunes");
        taskLunes.setDayOfWeek(1); // Lunes
        taskLunes.setTemplate(true);
        taskLunes.setDeadline(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        
        Task taskMiercoles = new Task();
        taskMiercoles.setTitle("Leer Miércoles");
        taskMiercoles.setDayOfWeek(3); // Miércoles
        taskMiercoles.setTemplate(true);
        taskMiercoles.setDeadline(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)));
        
        template.addTaskToTemplate(taskLunes);
        template.addTaskToTemplate(taskMiercoles);

        // Referencia: Un Jueves 14 de Marzo
        LocalDate referenceDate = LocalDate.of(2024, 3, 14);
        // El próximo lunes debería ser el 18 de Marzo

        // WHEN: Proyectamos
        List<Task> result = engine.projectNextWeek(template, referenceDate);

        // THEN:
        assertEquals(2, result.size());
        
        // Validar Lunes (18 de Marzo)
        Task projectedLunes = findByTitle(result, "Gym Lunes");
        assertNotNull(projectedLunes);
        assertEquals(LocalDate.of(2024, 3, 18), projectedLunes.getDeadline().toLocalDate());
        assertFalse(projectedLunes.isTemplate());
        assertNull(projectedLunes.id);

        // Validar Miércoles (20 de Marzo)
        Task projectedMiercoles = findByTitle(result, "Leer Miércoles");
        assertNotNull(projectedMiercoles);
        assertEquals(LocalDate.of(2024, 3, 20), projectedMiercoles.getDeadline().toLocalDate());
    }

    @Test
    public void testProjectNextWeekByCloningCurrentWeek() {
        // GIVEN: Tareas reales en la semana actual (Lunes 11 a Domingo 17 de Marzo)
        LocalDate mondayCurrent = LocalDate.of(2024, 3, 11);
        
        Task taskActual = new Task();
        taskActual.id = 99L; // Un ID existente
        taskActual.setTitle("Tarea Real");
        taskActual.setDeadline(LocalDateTime.of(mondayCurrent.plusDays(1), LocalTime.of(10, 0))); // Martes 12
        taskActual.setCompletada(true);
        
        List<Task> currentTasks = new ArrayList<>();
        currentTasks.add(taskActual);

        // Referencia: Jueves 14 de Marzo
        LocalDate referenceDate = LocalDate.of(2024, 3, 14);

        // WHEN: Clonamos la semana
        List<Task> result = engine.projectNextWeek(currentTasks, referenceDate);

        // THEN:
        assertEquals(1, result.size());
        Task projected = result.get(0);
        
        // Debe ser el martes de la PRÓXIMA semana (19 de Marzo)
        assertEquals(LocalDate.of(2024, 3, 19), projected.getDeadline().toLocalDate());
        assertEquals(LocalTime.of(10, 0), projected.getDeadline().toLocalTime());
        
        // Debe estar LIMPIA
        assertNull(projected.id);
        assertFalse(projected.isCompletada());
        assertFalse(projected.isTemplate());
    }

    private Task findByTitle(List<Task> tasks, String title) {
        for (Task t : tasks) {
            if (t.getTitle().equals(title)) return t;
        }
        return null;
    }
}
