package com.example.weekly.domain;

import java.time.LocalTime;

/**
 * Interfaz auxiliar para normalizar Tareas y Eventos en el motor de colisiones
 */
public interface TimeSlot {
    LocalTime getStartTime();
    LocalTime getEndTime();
}
