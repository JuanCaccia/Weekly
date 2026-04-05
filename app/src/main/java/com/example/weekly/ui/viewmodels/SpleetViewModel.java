package com.example.weekly.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.weekly.domain.CollisionDetector;
import com.example.weekly.domain.ProjectWeekUseCase;
import com.example.weekly.domain.SpleetHeader;
import com.example.weekly.domain.SpleetRepository;
import com.example.weekly.domain.SpleetTask;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SpleetViewModel extends AndroidViewModel {
    private final SpleetRepository repository;
    private final ProjectWeekUseCase projectWeekUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final CollisionDetector collisionDetector = new CollisionDetector();

    private final MutableLiveData<Long> _currentHeaderId = new MutableLiveData<>();
    public final LiveData<Long> currentHeaderId = _currentHeaderId;
    
    private final MediatorLiveData<List<SpleetTask>> _spleetTasks = new MediatorLiveData<>();
    public final LiveData<List<SpleetTask>> spleetTasks = _spleetTasks;
    
    public final LiveData<List<SpleetHeader>> allHeaders;
    
    private final MutableLiveData<List<Task>> _lastProjectedTasks = new MutableLiveData<>();
    public LiveData<List<Task>> lastProjectedTasks = _lastProjectedTasks;

    private final MutableLiveData<List<String>> _collisionNotifications = new MutableLiveData<>();
    public LiveData<List<String>> collisionNotifications = _collisionNotifications;

    @Inject
    public SpleetViewModel(Application application, SpleetRepository repository, ProjectWeekUseCase projectWeekUseCase) {
        super(application);
        this.repository = repository;
        this.projectWeekUseCase = projectWeekUseCase;
        
        this.allHeaders = repository.findAllHeadersLive();
        
        setupReactiveSpleetTasks();
        
        // Cargar el primer header disponible si existe
        executorService.execute(() -> {
            List<SpleetHeader> headers = repository.findAllHeaders();
            if (!headers.isEmpty()) {
                _currentHeaderId.postValue(headers.get(0).id);
            } else {
                // Crear uno por defecto si no hay ninguno
                ensureDefaultHeader();
            }
        });
    }

    private void ensureDefaultHeader() {
        SpleetHeader defaultHeader = new SpleetHeader(null, "Semana Ideal");
        repository.saveHeader(defaultHeader);
        _currentHeaderId.postValue(defaultHeader.id);
    }

    private void setupReactiveSpleetTasks() {
        LiveData<List<SpleetTask>> allSpleetTasksLive = repository.findAllLive();
        
        _spleetTasks.addSource(_currentHeaderId, headerId -> updateFilteredTasks(headerId, allSpleetTasksLive.getValue()));
        _spleetTasks.addSource(allSpleetTasksLive, tasks -> updateFilteredTasks(_currentHeaderId.getValue(), tasks));
    }

    private void updateFilteredTasks(Long headerId, List<SpleetTask> allTasks) {
        if (allTasks == null) {
            _spleetTasks.setValue(new ArrayList<>());
            return;
        }
        
        if (headerId == null) {
            _spleetTasks.setValue(allTasks);
            return;
        }
        
        List<SpleetTask> filtered = allTasks.stream()
                .filter(t -> Objects.equals(t.spleetHeaderId, headerId))
                .collect(Collectors.toList());
        _spleetTasks.setValue(filtered);
    }

    public void selectSpleet(Long headerId) {
        _currentHeaderId.setValue(headerId);
    }

    public void createNewSpleet(String name) {
        executorService.execute(() -> {
            SpleetHeader newHeader = new SpleetHeader(null, name);
            repository.saveHeader(newHeader);
            _currentHeaderId.postValue(newHeader.id);
        });
    }

    public void deleteCurrentSpleet() {
        deleteSpleetById(_currentHeaderId.getValue());
    }

    public void deleteSpleetById(Long idToDelete) {
        if (idToDelete == null) return;

        executorService.execute(() -> {
            List<SpleetHeader> headers = repository.findAllHeaders();
            SpleetHeader toDelete = headers.stream().filter(h -> h.id.equals(idToDelete)).findFirst().orElse(null);
            if (toDelete != null) {
                repository.deleteHeader(toDelete);
                
                // Si borramos el actual, buscar otro para seleccionar
                if (Objects.equals(idToDelete, _currentHeaderId.getValue())) {
                    List<SpleetHeader> remaining = repository.findAllHeaders();
                    if (!remaining.isEmpty()) {
                        _currentHeaderId.postValue(remaining.get(0).id);
                    } else {
                        ensureDefaultHeader();
                    }
                }
            }
        });
    }

    public Long getCurrentHeaderId() {
        return _currentHeaderId.getValue();
    }

    public void setSpleetName(String name) {
        Long currentId = _currentHeaderId.getValue();
        if (currentId != null) {
            executorService.execute(() -> {
                SpleetHeader header = new SpleetHeader(currentId, name);
                repository.saveHeader(header);
            });
        }
    }

    public void saveSpleetTask(SpleetTask task) {
        task.spleetHeaderId = _currentHeaderId.getValue();
        executorService.execute(() -> repository.save(task));
    }

    public boolean checkCollision(LocalTime start, LocalTime end, int dayOfWeek, Long excludeId) {
        try {
            Future<Boolean> future = executorService.submit(() -> {
                List<SpleetTask> allTasksInHeader = repository.findByHeader(_currentHeaderId.getValue());
                List<TimeSlot> slots = allTasksInHeader.stream()
                        .filter(t -> t.dayOfWeek == dayOfWeek && !Objects.equals(t.id, excludeId) && t.hasTimeBlock)
                        .map(t -> (TimeSlot) t)
                        .collect(Collectors.toList());
                return collisionDetector.hasCollision(start, end, slots);
            });
            return future.get();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteSpleetTask(SpleetTask task) {
        executorService.execute(() -> repository.delete(task));
    }

    public void applyToNextWeek() {
        applyToDate(LocalDate.now());
    }

    public void applyToDate(LocalDate referenceDate) {
        // Aseguramos que siempre se proyecte desde el Lunes de la semana seleccionada
        LocalDate targetMonday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        executorService.execute(() -> {
            List<SpleetTask> currentTemplate = repository.findByHeader(_currentHeaderId.getValue());
            if (!currentTemplate.isEmpty()) {
                ProjectWeekUseCase.ProjectionResult result = projectWeekUseCase.executeSpleet(currentTemplate, targetMonday);
                _lastProjectedTasks.postValue(result.savedTasks);
                if (!result.skippedTitles.isEmpty()) {
                    _collisionNotifications.postValue(result.skippedTitles);
                } else {
                    _collisionNotifications.postValue(new ArrayList<>()); // Éxito sin colisiones
                }
            }
        });
    }

    public void undoLastProjection() {
        List<Task> tasksToUndo = _lastProjectedTasks.getValue();
        if (tasksToUndo != null && !tasksToUndo.isEmpty()) {
            executorService.execute(() -> {
                projectWeekUseCase.undoProjection(tasksToUndo);
                _lastProjectedTasks.postValue(null);
            });
        }
    }

    public void clearProjectionState() {
        _lastProjectedTasks.setValue(null);
        _collisionNotifications.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
