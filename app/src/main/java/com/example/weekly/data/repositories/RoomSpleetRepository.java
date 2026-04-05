package com.example.weekly.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.weekly.data.local.dao.SpleetHeaderDao;
import com.example.weekly.data.local.dao.SpleetTaskDao;
import com.example.weekly.data.local.entities.SpleetHeaderEntity;
import com.example.weekly.data.local.entities.SpleetTaskEntity;
import com.example.weekly.data.mappers.SpleetHeaderMapper;
import com.example.weekly.data.mappers.SpleetTaskMapper;
import com.example.weekly.domain.SpleetHeader;
import com.example.weekly.domain.SpleetRepository;
import com.example.weekly.domain.SpleetTask;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class RoomSpleetRepository implements SpleetRepository {
    private final SpleetTaskDao taskDao;
    private final SpleetHeaderDao headerDao;

    @Inject
    public RoomSpleetRepository(SpleetTaskDao taskDao, SpleetHeaderDao headerDao) {
        this.taskDao = taskDao;
        this.headerDao = headerDao;
    }

    @Override
    public void save(SpleetTask task) {
        SpleetTaskEntity entity = SpleetTaskMapper.toEntity(task);
        if (entity.id == null) {
            taskDao.insert(entity);
        } else {
            taskDao.update(entity);
        }
    }

    @Override
    public void delete(SpleetTask task) {
        taskDao.delete(SpleetTaskMapper.toEntity(task));
    }

    @Override
    public List<SpleetTask> findAll() {
        return taskDao.findAll().stream()
                .map(SpleetTaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<SpleetTask>> findAllLive() {
        return Transformations.map(taskDao.findAllLive(), entities ->
                entities.stream()
                        .map(SpleetTaskMapper::toDomain)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<SpleetTask> findByHeader(Long headerId) {
        return taskDao.findByHeader(headerId).stream()
                .map(SpleetTaskMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<SpleetTask>> findByHeaderLive(Long headerId) {
        return Transformations.map(taskDao.findByHeaderLive(headerId), entities ->
                entities.stream()
                        .map(SpleetTaskMapper::toDomain)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void saveHeader(SpleetHeader header) {
        SpleetHeaderEntity entity = SpleetHeaderMapper.toEntity(header);
        if (entity.id == null) {
            long id = headerDao.insert(entity);
            header.id = id;
        } else {
            headerDao.update(entity);
        }
    }

    @Override
    public void deleteHeader(SpleetHeader header) {
        headerDao.delete(SpleetHeaderMapper.toEntity(header));
    }

    @Override
    public List<SpleetHeader> findAllHeaders() {
        return headerDao.findAll().stream()
                .map(SpleetHeaderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public LiveData<List<SpleetHeader>> findAllHeadersLive() {
        return Transformations.map(headerDao.findAllLive(), entities ->
                entities.stream()
                        .map(SpleetHeaderMapper::toDomain)
                        .collect(Collectors.toList())
        );
    }
}
