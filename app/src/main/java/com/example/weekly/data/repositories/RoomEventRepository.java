package com.example.weekly.data.repositories;

import com.example.weekly.data.local.dao.EventDao;
import com.example.weekly.data.local.entities.EventEntity;
import com.example.weekly.data.mappers.EventMapper;
import com.example.weekly.domain.Event;
import com.example.weekly.domain.EventRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomEventRepository implements EventRepository {
    private final EventDao eventDao;

    public RoomEventRepository(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public Event save(Event event) {
        EventEntity entity = EventMapper.toEntity(event);
        eventDao.insertEvent(entity);
        return event;
    }

    @Override
    public void deleteById(Long id) {
        eventDao.deleteById(id);
    }

    @Override
    public Optional<Event> findById(Long id) {
        EventEntity entity = eventDao.findById(id);
        return Optional.ofNullable(EventMapper.toDomain(entity));
    }

    @Override
    public List<Event> findByDate(LocalDate date) {
        String dateQuery = date.toString() + "%";
        return eventDao.getEventsByDay(dateQuery).stream()
                .map(EventMapper::toDomain)
                .collect(Collectors.toList());
    }
}
