package com.example.weekly.ui.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.example.weekly.ui.adapters.DayAdapter;
import com.example.weekly.ui.dialogs.AddTaskDialogFragment;
import com.example.weekly.ui.dialogs.TaskOptionsBottomSheet;
import com.example.weekly.ui.models.DayPlan;
import com.example.weekly.ui.viewmodels.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WeekFragment extends Fragment implements DayAdapter.OnDayPlanInteractionListener {
    private MainViewModel viewModel;
    private DayAdapter adapter;
    private RecyclerView rv;
    private static final String DIALOG_TAG = "task_dialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    // Configuración para Auto-Scroll durante Drag & Drop
    private final android.os.Handler scrollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private int scrollSpeed = 0;
    private boolean isScrolling = false;

    // Configuración para Navegación Horizontal durante Drag
    private final android.os.Handler navHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable navRunnable = null;
    private int pendingNavDir = 0;
    private static final int NAV_DELAY = 500;

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (scrollSpeed != 0 && rv != null) {
                rv.scrollBy(0, scrollSpeed);
                scrollHandler.postDelayed(this, 16);
            } else {
                isScrolling = false;
            }
        }
    };

    /**
     * Procesa la ubicación del drag para determinar la velocidad del scroll.
     * Centralizamos esto para poder llamarlo desde el Fragment o desde los hijos.
     */
    public void handleDragScroll(float rawY) {
        if (rv == null) return;
        
        int[] location = new int[2];
        rv.getLocationOnScreen(location);
        int rvTop = location[1];
        int rvHeight = rv.getHeight();
        
        // Coordenada Y relativa al RecyclerView
        float relativeY = rawY - rvTop;
        
        int threshold = dpToPx(50); 
        
        int newSpeed = 0;
        if (relativeY < threshold && relativeY > -dpToPx(100)) {
            float intensity = (threshold - Math.max(0, relativeY)) / (float) threshold;
            newSpeed = - (int) (intensity * dpToPx(30)) - dpToPx(5);
        } else if (relativeY > rvHeight - threshold && relativeY < rvHeight + dpToPx(100)) {
            float intensity = (relativeY - (rvHeight - threshold)) / (float) threshold;
            newSpeed = (int) (intensity * dpToPx(30)) + dpToPx(5);
        }
        
        scrollSpeed = newSpeed;
        if (scrollSpeed != 0) {
            if (!isScrolling) {
                isScrolling = true;
                scrollHandler.post(scrollRunnable);
            }
        } else {
            isScrolling = false;
            scrollHandler.removeCallbacks(scrollRunnable);
        }
    }

    private void handleHorizontalNav(float x, int width) {
        int threshold = dpToPx(40);
        if (x < threshold) {
            scheduleNav(-1);
        } else if (x > width - threshold) {
            scheduleNav(1);
        } else {
            cancelHorizontalNav();
        }
    }

    private void scheduleNav(int direction) {
        if (pendingNavDir == direction) return;
        cancelHorizontalNav();
        pendingNavDir = direction;
        navRunnable = () -> {
            if (direction == -1) viewModel.prevWeek();
            else viewModel.nextWeek();
            pendingNavDir = 0;
        };
        navHandler.postDelayed(navRunnable, NAV_DELAY);
    }

    private void cancelHorizontalNav() {
        if (navRunnable != null) {
            navHandler.removeCallbacks(navRunnable);
            navRunnable = null;
        }
        pendingNavDir = 0;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        rv = view.findViewById(R.id.rvWeekPlan);
        adapter = new DayAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Detector de scroll en el contenedor raíz
        view.setOnDragListener((v, event) -> {
            if (event.getAction() == android.view.DragEvent.ACTION_DRAG_LOCATION) {
                int[] loc = new int[2];
                v.getLocationOnScreen(loc);
                handleDragScroll(loc[1] + event.getY());
                handleHorizontalNav(event.getX(), v.getWidth());
            } else if (event.getAction() == android.view.DragEvent.ACTION_DRAG_ENDED || 
                       event.getAction() == android.view.DragEvent.ACTION_DROP) {
                scrollSpeed = 0;
                isScrolling = false;
                scrollHandler.removeCallbacks(scrollRunnable);
                cancelHorizontalNav();
            }
            return false;
        });

        // El RecyclerView también debe procesar su propia área por si el evento no burbujea
        rv.setOnDragListener((v, event) -> {
            if (event.getAction() == android.view.DragEvent.ACTION_DRAG_LOCATION) {
                int[] loc = new int[2];
                v.getLocationOnScreen(loc);
                handleDragScroll(loc[1] + event.getY());
                
                int[] rootLoc = new int[2];
                view.getLocationOnScreen(rootLoc);
                float rootX = (loc[0] + event.getX()) - rootLoc[0];
                handleHorizontalNav(rootX, view.getWidth());
            } else if (event.getAction() == android.view.DragEvent.ACTION_DRAG_ENDED || 
                       event.getAction() == android.view.DragEvent.ACTION_DROP) {
                cancelHorizontalNav();
            }
            return false;
        });

        // Desactivar animaciones por defecto para que no interfieran con TransitionManager
        rv.setItemAnimator(null);

        viewModel.weekPlan.observe(getViewLifecycleOwner(), plan -> {
            if (plan != null) {
                // Al enviar la lista, evitamos el scroll automático si viene de un Drag & Drop
                // Solo hacemos scroll si no estamos en medio de un proceso de arrastre
                adapter.submitList(plan, null);
            }
        });

        // Configuración de navegación de semanas
        TextView textWeek = view.findViewById(R.id.textCurrentWeek);
        view.findViewById(R.id.btnPrevWeek).setOnClickListener(v -> viewModel.prevWeek());
        view.findViewById(R.id.btnNextWeek).setOnClickListener(v -> viewModel.nextWeek());
        textWeek.setOnClickListener(v -> viewModel.currentWeek());

        viewModel.weekOffset.observe(getViewLifecycleOwner(), offset -> {
            textWeek.setText(formatWeekTitle(offset));
        });

        setupFAB(view);
    }

    private String formatWeekTitle(int offset) {
        if (offset == 0) return "Esta semana";
        if (offset == 1) return "Próxima semana";
        if (offset == -1) return "Semana pasada";

        LocalDate date = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(offset);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return "Semana del " + date.format(formatter);
    }

    private void setupFAB(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            if (getParentFragmentManager().findFragmentByTag(DIALOG_TAG) != null) return;

            LocalDate targetDate = LocalDate.now();
            List<DayPlan> currentPlan = viewModel.weekPlan.getValue();
            if (currentPlan != null) {
                for (DayPlan dp : currentPlan) {
                    if (dp.isExpanded() && dp.getType() == DayPlan.Type.NORMAL) {
                        targetDate = dp.getDate();
                        break;
                    }
                }
            }
            AddTaskDialogFragment.newInstance(targetDate)
                    .show(getParentFragmentManager(), DIALOG_TAG);
        });
    }

    @Override
    public void onToggleExpansion(LocalDate date) {
        viewModel.toggleDayExpansion(date);
    }

    public void onAddTask(DayPlan plan) {
        if (getParentFragmentManager().findFragmentByTag(DIALOG_TAG) != null) return;
        AddTaskDialogFragment.newInstance(plan.getDate())
                .show(getParentFragmentManager(), DIALOG_TAG);
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        Task updatedTask = task.copy();
        updatedTask.setCompletada(isCompleted);
        viewModel.saveTask(updatedTask);
    }

    @Override
    public void onTaskDelete(Task task) {
        viewModel.deleteTask(task);
    }

    @Override
    public void onTaskEdit(Task task) {
        if (getParentFragmentManager().findFragmentByTag(DIALOG_TAG) != null) return;

        if (task.isHasTimeBlock()) {
            openEditDialog(task);
        } else {
            showTaskOptionsDialog(task);
        }
    }

    private void showTaskOptionsDialog(Task task) {
        TaskOptionsBottomSheet bottomSheet = new TaskOptionsBottomSheet(task, new TaskOptionsBottomSheet.OnTaskOptionListener() {
            @Override
            public void onEdit(Task task) {
                openEditDialog(task);
            }

            @Override
            public void onToggleStatus(Task task) {
                onTaskStatusChanged(task, !task.isCompletada());
            }

            @Override
            public void onDelete(Task task) {
                onTaskDelete(task);
            }
        });
        bottomSheet.show(getParentFragmentManager(), "task_options");
    }

    private void openEditDialog(Task task) {
        LocalDate date = task.getDeadline() != null ? task.getDeadline().toLocalDate() : LocalDate.now();
        AddTaskDialogFragment.newInstanceForEdit(date, task.id)
                .show(getParentFragmentManager(), DIALOG_TAG);
    }

    @Override
    public void onTaskArchive(Task task) {}

    @Override
    public void onRescheduleAllRemains() {}

    @Override
    public void onArchiveAllRemains() {}

    @Override
    public void onTaskDropped(Long taskId, java.time.LocalTime newStartTime, LocalDate date) {
        viewModel.onTaskDropped(taskId, newStartTime, date);
    }
}
