package com.example.weekly.domain;

import androidx.lifecycle.LiveData;
import java.util.List;
import javax.inject.Inject;

/**
 * Use case to retrieve all archived tasks.
 */
public class GetArchivedTasksUseCase {
    private final TaskRepository repository;

    @Inject
    public GetArchivedTasksUseCase(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns a LiveData list of all archived tasks.
     */
    public LiveData<List<Task>> executeLive() {
        return repository.findArchivedLive();
    }

    /**
     * Returns a list of all archived tasks (synchronous).
     */
    public List<Task> execute() {
        return repository.findArchived();
    }
}
