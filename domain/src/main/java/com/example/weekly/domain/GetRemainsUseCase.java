package com.example.weekly.domain;

import androidx.lifecycle.LiveData;
import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Inject;

/**
 * Orquestador para obtener las tareas pendientes (Remains).
 * Se consideran "Remains" aquellas tareas que no tienen bloque de tiempo
 * o que ya han vencido.
 */
public class GetRemainsUseCase {
    private final TaskRepository repository;

    @Inject
    public GetRemainsUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene LiveData de todas las tareas no completadas (Remains).
     */
    public LiveData<List<Task>> executeLive() {
        return repository.findRemainsLive();
    }

    /**
     * Obtiene lista síncrona de todas las tareas no completadas (Remains).
     */
    public List<Task> execute() {
        return repository.findRemains();
    }
}
