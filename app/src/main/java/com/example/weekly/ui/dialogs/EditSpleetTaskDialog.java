package com.example.weekly.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.weekly.R;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.SpleetTask;
import com.example.weekly.ui.viewmodels.SpleetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EditSpleetTaskDialog extends BottomSheetDialogFragment {

    private static final String ARG_TASK_ID = "arg_task_id";
    
    private Long editingTaskId = null;
    private SpleetTask editingTask = null;
    
    private LocalTime startTime = LocalTime.of(9, 0);
    private LocalTime endTime = LocalTime.of(10, 0);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    private int selectedDay = 1;

    private TextInputEditText editTitle;

    public static EditSpleetTaskDialog newInstance() {
        return new EditSpleetTaskDialog();
    }

    public static EditSpleetTaskDialog newInstanceForEdit(Long taskId) {
        EditSpleetTaskDialog fragment = new EditSpleetTaskDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_TASK_ID)) {
            editingTaskId = getArguments().getLong(ARG_TASK_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_edit_spleet_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SpleetViewModel viewModel = new ViewModelProvider(requireActivity()).get(SpleetViewModel.class);
        
        TextView textTitleDialog = view.findViewById(R.id.textDialogTitle);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupType);
        TextInputLayout layoutTitle = view.findViewById(R.id.layoutTaskTitle);
        editTitle = view.findViewById(R.id.editTaskTitle);
        ChipGroup chipGroupDay = view.findViewById(R.id.chipGroupDay);
        
        SwitchMaterial switchTimeBlock = view.findViewById(R.id.switchTimeBlock);
        TextInputLayout layoutStartTime = view.findViewById(R.id.layoutStartTime);
        TextInputLayout layoutEndTime = view.findViewById(R.id.layoutEndTime);
        Button btnStartTime = view.findViewById(R.id.btnStartTime);
        Button btnEndTime = view.findViewById(R.id.btnEndTime);
        
        TextView textPriorityHeader = view.findViewById(R.id.textPriorityHeader);
        View scrollPriority = view.findViewById(R.id.scrollPriority);
        ChipGroup chipGroupPriority = view.findViewById(R.id.chipGroupPriority);
        
        Button btnSave = view.findViewById(R.id.btnSaveSpleet);
        Button btnDelete = view.findViewById(R.id.btnDeleteSpleet);

        setupDayChips(chipGroupDay);
        setupPriorityChips(chipGroupPriority);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTypeEvent) {
                    textPriorityHeader.setVisibility(View.GONE);
                    scrollPriority.setVisibility(View.GONE);
                } else {
                    textPriorityHeader.setVisibility(View.VISIBLE);
                    scrollPriority.setVisibility(View.VISIBLE);
                }
            }
        });

        switchTimeBlock.setOnCheckedChangeListener((v, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            layoutStartTime.setVisibility(visibility);
            layoutEndTime.setVisibility(visibility);
        });

        btnStartTime.setOnClickListener(v -> {
            prepareForTimePicker();
            showTimePicker(true, btnStartTime);
        });
        btnEndTime.setOnClickListener(v -> {
            prepareForTimePicker();
            showTimePicker(false, btnEndTime);
        });

        if (editingTaskId != null) {
            textTitleDialog.setText("Editar Actividad");
            btnDelete.setVisibility(View.VISIBLE);
            
            if (viewModel.spleetTasks.getValue() != null) {
                for (SpleetTask t : viewModel.spleetTasks.getValue()) {
                    if (Objects.equals(t.id, editingTaskId)) {
                        editingTask = t;
                        break;
                    }
                }
            }

            if (editingTask != null) {
                editTitle.setText(editingTask.title);
                selectedDay = editingTask.dayOfWeek;
                selectDayChip(chipGroupDay, selectedDay);
                
                switchTimeBlock.setChecked(editingTask.hasTimeBlock);
                if (editingTask.hasTimeBlock) {
                    startTime = editingTask.startTime;
                    endTime = editingTask.endTime;
                    btnStartTime.setText(startTime.format(timeFormatter));
                    btnEndTime.setText(endTime.format(timeFormatter));
                }

                if (editingTask.priority == null) {
                    toggleGroup.check(R.id.btnTypeEvent);
                } else {
                    toggleGroup.check(R.id.btnTypeTask);
                    selectPriorityChip(chipGroupPriority, editingTask.priority);
                }
            }
        } else {
            textTitleDialog.setText("Nueva Actividad");
            btnStartTime.setText(startTime.format(timeFormatter));
            btnEndTime.setText(endTime.format(timeFormatter));
        }

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                layoutTitle.setError("El título es obligatorio");
                return;
            }

            if (switchTimeBlock.isChecked()) {
                if (!startTime.isBefore(endTime)) {
                    Toast.makeText(getContext(), "La hora de inicio debe ser anterior a la de fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (viewModel.checkCollision(startTime, endTime, selectedDay, editingTaskId)) {
                    Toast.makeText(getContext(), "¡Atención! Este horario ya está ocupado en tu semana ideal", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean isEvent = toggleGroup.getCheckedButtonId() == R.id.btnTypeEvent;
            Priority priority = null;
            if (!isEvent) {
                priority = getSelectedPriority(chipGroupPriority);
            }

            SpleetTask task = editingTask != null ? editingTask : new SpleetTask();
            task.title = title;
            task.dayOfWeek = selectedDay;
            task.priority = priority;
            task.hasTimeBlock = switchTimeBlock.isChecked();
            if (task.hasTimeBlock) {
                task.startTime = startTime;
                task.endTime = endTime;
            } else {
                task.startTime = null;
                task.endTime = null;
            }

            viewModel.saveSpleetTask(task);
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (editingTask != null) {
                viewModel.deleteSpleetTask(editingTask);
                dismiss();
            }
        });
    }

    private void prepareForTimePicker() {
        if (editTitle != null) {
            editTitle.clearFocus();
            hideKeyboard(editTitle);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupDayChips(ChipGroup group) {
        group.removeAllViews();
        String[] labels = {"L", "M", "M", "J", "V", "S", "D"};
        for (int i = 0; i < 7; i++) {
            int dayNum = i + 1;
            Chip chip = new Chip(getContext());
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            if (dayNum == selectedDay) chip.setChecked(true);
            chip.setOnCheckedChangeListener((v, isChecked) -> {
                if (isChecked) selectedDay = dayNum;
            });
            group.addView(chip);
        }
    }

    private void setupPriorityChips(ChipGroup group) {
        group.removeAllViews();
        addPriorityChip(group, Priority.LOW, "Baja", R.color.priority_low);
        addPriorityChip(group, Priority.MEDIUM, "Media", R.color.priority_medium);
        addPriorityChip(group, Priority.HIGH, "Alta", R.color.priority_high);
    }

    private void addPriorityChip(ChipGroup group, Priority priority, String label, int colorRes) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_priority_chip, group, false);
        chip.setText(label);
        chip.setId(View.generateViewId());
        chip.setTag(priority);
        int color = ContextCompat.getColor(requireContext(), colorRes);
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}};
        int[] colors = new int[]{color, ContextCompat.getColor(requireContext(), R.color.white)};
        chip.setChipBackgroundColor(new ColorStateList(states, colors));
        chip.setChipStrokeColor(ColorStateList.valueOf(color));
        group.addView(chip);
        if (priority == Priority.MEDIUM) chip.setChecked(true);
    }

    private void selectDayChip(ChipGroup group, int day) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (i == day - 1) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void selectPriorityChip(ChipGroup group, Priority priority) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.getTag() == priority) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private Priority getSelectedPriority(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        Chip chip = group.findViewById(checkedId);
        if (chip != null) return (Priority) chip.getTag();
        return Priority.MEDIUM;
    }

    private void showTimePicker(boolean isStart, Button btn) {
        LocalTime current = isStart ? startTime : endTime;
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(current.getHour())
                .setMinute(current.getMinute())
                .setTitleText(isStart ? "Hora de inicio" : "Hora de fin")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK) // Forzar modo reloj (círculo)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            LocalTime newTime = LocalTime.of(picker.getHour(), picker.getMinute());
            if (isStart) {
                startTime = newTime;
                if (endTime.isBefore(startTime)) {
                    endTime = startTime.plusHours(1);
                }
            } else {
                endTime = newTime;
            }
            btn.setText(newTime.format(timeFormatter));
        });

        picker.show(getChildFragmentManager(), "time_picker_spleet");
    }
}
