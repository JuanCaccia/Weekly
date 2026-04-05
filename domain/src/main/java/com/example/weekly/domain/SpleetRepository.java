package com.example.weekly.domain;

import androidx.lifecycle.LiveData;
import java.util.List;

public interface SpleetRepository {
    // Tasks
    void save(SpleetTask task);
    void delete(SpleetTask task);
    List<SpleetTask> findAll();
    LiveData<List<SpleetTask>> findAllLive();
    List<SpleetTask> findByHeader(Long headerId);
    LiveData<List<SpleetTask>> findByHeaderLive(Long headerId);

    // Headers
    void saveHeader(SpleetHeader header);
    void deleteHeader(SpleetHeader header);
    List<SpleetHeader> findAllHeaders();
    LiveData<List<SpleetHeader>> findAllHeadersLive();
}
