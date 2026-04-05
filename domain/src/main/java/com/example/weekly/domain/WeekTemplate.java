package com.example.weekly.domain;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class WeekTemplate {
    private final Long id;
    private final String name;
    private final List<Task> templateTasks; // Tareas con es_semana_tipo = true

    public WeekTemplate(Long id, String name) {
        this.id = id;
        this.name = name;
        this.templateTasks = new ArrayList<>();
    }

    public void addTaskToTemplate(Task task) {
        if (task != null && task.isTemplate()) {
            templateTasks.add(task);
        }
    }

    public List<Task> getTemplateTasks() {
        return new ArrayList<>(templateTasks);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}