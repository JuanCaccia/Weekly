package com.example.weekly.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);
    void deleteById(Long id);
    Optional<Event> findById(Long id);
    List<Event> findByDate(LocalDate date);
}
