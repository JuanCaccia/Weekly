package com.example.weekly.domain;

import java.util.Objects;

/**
 * Representa la cabecera de un Spleet (un conjunto de tareas plantilla).
 */
public class SpleetHeader {
    public Long id;
    public String name;

    public SpleetHeader() {}

    public SpleetHeader(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpleetHeader that = (SpleetHeader) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
