package com.example.weekly.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weekly.domain.CollisionDetector;
import com.example.weekly.domain.EventRepository;
import com.example.weekly.domain.GetRemainsUseCase;
import com.example.weekly.domain.HeatmapService;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskRepository;
import com.example.weekly.domain.TimeSlot;
import com.example.weekly.ui.models.DayPlan;
import com.example.weekly.ui.usecases.ProjectWeekUseCase;
import com.example.weekly.utils.TaskAlarmScheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
public class MainViewModel extends ViewModel {
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ProjectWeekUseCase projectWeekUseCase;
    private final GetRemainsUseCase getRemainsUseCase;
    private final HeatmapService heatmapService;
    private final TaskAlarmScheduler taskAlarmScheduler;
    private final CollisionDetector collisionDetector = new CollisionDetector();
    
    private final MediatorLiveData<List<DayPlan>> _weekPlan = new MediatorLiveData<>();
    public LiveData<List<DayPlan>> weekPlan = _weekPlan;
    
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<LocalDate> _expandedDate = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _isRemainsExpanded = new MutableLiveData<>(false);
    
    // Control de navegación de semanas
    private final MutableLiveData<Integer> _weekOffset = new MutableLiveData<>(0);
    public LiveData<Integer> weekOffset = _weekOffset;
    
    public final LiveData<List<Task>> remains;
    public final LiveData<List<Task>> archivedTasks;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public MainViewModel(TaskRepository taskRepository, 
                         EventRepository eventRepository,
                         ProjectWeekUseCase projectWeekUseCase,
                         GetRemainsUseCase getRemainsUseCase,
                         HeatmapService heatmapService,
                         TaskAlarmScheduler taskAlarmScheduler) {
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.projectWeekUseCase = projectWeekUseCase;
        this.getRemainsUseCase = getRemainsUseCase;
        this.heatmapService = heatmapService;
        this.taskAlarmScheduler = taskAlarmScheduler;
        
        this.remains = getRemainsUseCase.executeLive();
        this.archivedTasks = taskRepository.findArchivedLive();
        
        setupReactiveWeekPlan();
    }

    private void setupReactiveWeekPlan() {
        LiveData<List<Task>> tasksLive = taskRepository.findAllLive();
        
        _weekPlan.addSource(tasksLive, tasks -> updateWeekPlan(tasks, _expandedDate.getValue(), _weekOffset.getValue()));
        _weekPlan.addSource(_expandedDate, date -> updateWeekPlan(tasksLive.getValue(), date, _weekOffset.getValue()));
        _weekPlan.addSource(_weekOffset, offset -> updateWeekPlan(tasksLive.getValue(), _expandedDate.getValue(), offset));
    }

    private void updateWeekPlan(List<Task> allTasks, LocalDate expandedDate, int offset) {
        if (allTasks == null) return;

        executorService.execute(() -> {
            List<DayPlan> finalPlan = new ArrayList<>();

            LocalDate monday = LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .plusWeeks(offset);
            
            for (int i = 0; i < 7; i++) {
                LocalDate current = monday.plusDays(i);
                
                List<Task> dayTasks = allTasks.stream()
                        .filter(t -> t.getDeadline() != null && t.getDeadline().toLocalDate().equals(current))
                        .collect(Collectors.toList());
                
                DayPlan dayPlan = new DayPlan(current, dayTasks);
                HeatmapService.DensityResult result = heatmapService.calculateDensity(new ArrayList<>(dayTasks));
                dayPlan.setDensityLevel(result.level);
                
                if (current.equals(expandedDate)) {
                    dayPlan.setExpanded(true);
                }
                
                finalPlan.add(dayPlan);
            }
            _weekPlan.postValue(finalPlan);
        });
    }

    public LiveData<List<Task>> findAllLive() {
        return taskRepository.findAllLive();
    }

    public LiveData<List<Task>> getTasksBetweenDates(LocalDate start, LocalDate end) {
        return taskRepository.findTasksBetweenDatesLive(start, end);
    }

    public void nextWeek() {
        Integer current = _weekOffset.getValue();
        _weekOffset.setValue(current != null ? current + 1 : 1);
    }

    public void prevWeek() {
        Integer current = _weekOffset.getValue();
        _weekOffset.setValue(current != null ? current - 1 : -1);
    }

    public void currentWeek() {
        _weekOffset.setValue(0);
    }

    public void navigateToDate(LocalDate date) {
        // Calcular el offset de semanas desde la semana actual
        LocalDate todayMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate targetMonday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        long weeksBetween = ChronoUnit.WEEKS.between(todayMonday, targetMonday);
        
        _weekOffset.setValue((int) weeksBetween);
        _expandedDate.setValue(date);
    }

    public void moveTaskToDate(Task task, LocalDate date) {
        executorService.execute(() -> {
            if (task.getDeadline() != null) {
                task.setDeadline(LocalDateTime.of(date, task.getDeadline().toLocalTime()));
            } else {
                task.setDeadline(LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT));
            }
            Task updatedTask = taskRepository.save(task);
            if (updatedTask != null) {
                taskAlarmScheduler.scheduleTaskAlarm(updatedTask);
            }
        });
    }

    public void saveTask(Task task) {
        executorService.execute(() -> {
            Task savedTask = taskRepository.save(task);
            if (savedTask != null) {
                taskAlarmScheduler.scheduleTaskAlarm(savedTask);
            }
        });
    }

    public void addTask(Task task) {
        saveTask(task);
    }

    public void updateTask(Task task) {
        saveTask(task);
    }

    public Task getTaskById(Long id) {
        try {
            Future<Task> future = executorService.submit(() -> taskRepository.findById(id).orElse(null));
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean checkCollision(LocalTime start, LocalTime end, LocalDate date, Long excludeId) {
        try {
            Future<Boolean> future = executorService.submit(() -> {
                List<Task> dayTasks = taskRepository.findByDate(date);
                List<TimeSlot> slots = dayTasks.stream()
                        .filter(t -> !Objects.equals(t.id, excludeId) && t.isHasTimeBlock())
                        .map(t -> (TimeSlot) t)
                        .collect(Collectors.toList());
                return collisionDetector.hasCollision(start, end, slots);
            });
            return future.get();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> {
            if (task.id != null) {
                taskAlarmScheduler.cancelAlarm(task);
                taskRepository.deleteById(task.id);
            }
        });
    }

    public void deleteTask(Long taskId) {
        executorService.execute(() -> {
            taskRepository.findById(taskId).ifPresent(task -> {
                taskAlarmScheduler.cancelAlarm(task);
                taskRepository.deleteById(taskId);
            });
        });
    }

    public void unarchiveTask(Task task) {
        executorService.execute(() -> {
            task.setCompletada(false);
            task.setArchivedAt(null);
            Task updatedTask = taskRepository.save(task);
            if (updatedTask != null) {
                taskAlarmScheduler.scheduleTaskAlarm(updatedTask);
            }
        });
    }

    public void toggleDayExpansion(LocalDate date) {
        if (date == null) {
            Boolean current = _isRemainsExpanded.getValue();
            _isRemainsExpanded.setValue(current == null || !current);
            return;
        }
        
        LocalDate currentExpanded = _expandedDate.getValue();
        if (date.equals(currentExpanded)) {
            _expandedDate.setValue(null);
        } else {
            _expandedDate.setValue(date);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
