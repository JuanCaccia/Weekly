package com.example.weekly.ui.fragments;

import android.os.Bundle;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        rv = view.findViewById(R.id.rvWeekPlan);
        adapter = new DayAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Desactivar animaciones por defecto para que no interfieran con TransitionManager
        rv.setItemAnimator(null);

        viewModel.weekPlan.observe(getViewLifecycleOwner(), plan -> {
            if (plan != null) {
                // Eliminamos el smoothScrollToPosition que causaba inconsistencia visual durante la expansión
                adapter.submitList(plan);
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
}
