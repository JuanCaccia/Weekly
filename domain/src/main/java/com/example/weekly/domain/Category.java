package com.example.weekly.domain;

import java.util.Objects;

public class Category {
    private final Long id;
    private final String name;
    private final String colorHex;

    public Category(Long id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(name, category.name) && Objects.equals(colorHex, category.colorHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, colorHex);
    }
}
