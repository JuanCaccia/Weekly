package com.example.weekly.ui.dialogs;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.weekly.R;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.Task;
import com.example.weekly.domain.TaskReminder;
import com.example.weekly.ui.viewmodels.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddTaskDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_DATE = "arg_date";
    private static final String ARG_TASK_ID = "arg_task_id";

    private LocalDate selectedDate;
    private Long editingTaskId = null;
    private LocalTime startTime = LocalTime.of(9, 0);
    private LocalTime endTime = LocalTime.of(10, 0);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private List<TaskReminder> tempReminders = new ArrayList<>();

    public static AddTaskDialogFragment newInstance(LocalDate date) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddTaskDialogFragment newInstance(LocalDate date, long taskId) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putLong(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddTaskDialogFragment newInstanceForEdit(LocalDate date, long taskId) {
        return newInstance(date, taskId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = (LocalDate) getArguments().getSerializable(ARG_DATE);
            if (getArguments().containsKey(ARG_TASK_ID)) {
                editingTaskId = getArguments().getLong(ARG_TASK_ID);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        TextView textTitleDialog = view.findViewById(R.id.textDialogTitle);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupType);
        TextInputLayout layoutTitle = view.findViewById(R.id.layoutTaskTitle);
        TextInputEditText editTitle = view.findViewById(R.id.editTaskTitle);
        SwitchMaterial switchTimeBlock = view.findViewById(R.id.switchTimeBlock);
        
        TextInputLayout layoutStartTime = view.findViewById(R.id.layoutStartTime);
        TextInputLayout layoutEndTime = view.findViewById(R.id.layoutEndTime);
        Button btnStartTime = view.findViewById(R.id.btnStartTime);
        Button btnEndTime = view.findViewById(R.id.btnEndTime);
        
        SwitchMaterial switchImportant = view.findViewById(R.id.switchImportant);
        View scrollPalette = view.findViewById(R.id.scrollPalette);
        ChipGroup chipGroupPalette = view.findViewById(R.id.chipGroupPalette);

        TextView textPriorityHeader = view.findViewById(R.id.textPriorityHeader);
        View scrollPriority = view.findViewById(R.id.scrollPriority);
        ChipGroup chipGroupPriority = view.findViewById(R.id.chipGroupPriority);
        
        Button btnReminders = view.findViewById(R.id.btnReminders);
        Button btnSave = view.findViewById(R.id.btnSaveTask);
        Button btnDelete = view.findViewById(R.id.btnDeleteTask);

        LocalDate dateToUse = selectedDate != null ? selectedDate : LocalDate.now();

        String dayOfWeek = dateToUse.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String dateFormatted = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1) + " " + dateToUse.getDayOfMonth();
        textTitleDialog.setText("Nueva Actividad - " + dateFormatted);

        setupPriorityChips(chipGroupPriority);
        setupPaletteChips(chipGroupPalette);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTypeEvent) {
                    textPriorityHeader.setVisibility(View.GONE);
                    scrollPriority.setVisibility(View.GONE);
                    switchImportant.setVisibility(View.VISIBLE);
                } else {
                    textPriorityHeader.setVisibility(View.VISIBLE);
                    scrollPriority.setVisibility(View.VISIBLE);
                    switchImportant.setVisibility(View.GONE);
                    switchImportant.setChecked(false);
                    scrollPalette.setVisibility(View.GONE);
                }
            }
        });

        switchImportant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scrollPalette.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        switchTimeBlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            layoutStartTime.setVisibility(visibility);
            layoutEndTime.setVisibility(visibility);
        });

        btnStartTime.setText(startTime.format(timeFormatter));
        btnEndTime.setText(endTime.format(timeFormatter));

        btnStartTime.setOnClickListener(v -> {
            editTitle.clearFocus();
            showTimePicker(true, btnStartTime);
        });
        btnEndTime.setOnClickListener(v -> {
            editTitle.clearFocus();
            showTimePicker(false, btnEndTime);
        });

        btnReminders.setOnClickListener(v -> {
            RemindersDialogFragment remindersDialog = RemindersDialogFragment.newInstance(
                    tempReminders, 
                    switchTimeBlock.isChecked(), 
                    dateToUse
            );
            remindersDialog.setOnRemindersSelectedListener(reminders -> {
                tempReminders = reminders;
                updateRemindersButtonText(btnReminders);
            });
            remindersDialog.show(getChildFragmentManager(), "reminders_dialog");
        });

        // LOGICA DE CARGA PARA EDICIÓN
        if (editingTaskId != null) {
            textTitleDialog.setText("Editar Actividad");
            btnDelete.setVisibility(View.VISIBLE);
            btnSave.setText("ACTUALIZAR");
            
            Task task = viewModel.getTaskById(editingTaskId);
            if (task != null) {
                editTitle.setText(task.getTitle());
                switchTimeBlock.setChecked(task.isHasTimeBlock());
                if (task.isHasTimeBlock() && task.getStartTime() != null && task.getEndTime() != null) {
                    startTime = task.getStartTime();
                    endTime = task.getEndTime();
                    btnStartTime.setText(startTime.format(timeFormatter));
                    btnEndTime.setText(endTime.format(timeFormatter));
                }
                
                tempReminders = new ArrayList<>(task.getReminders());
                updateRemindersButtonText(btnReminders);

                if (task.isEvent()) {
                    toggleGroup.check(R.id.btnTypeEvent);
                    switchImportant.setVisibility(View.VISIBLE);
                    switchImportant.setChecked(task.isImportant());
                    if (task.isImportant()) {
                        scrollPalette.setVisibility(View.VISIBLE);
                        selectPaletteChip(chipGroupPalette, task.getImportantColor());
                    }
                } else {
                    toggleGroup.check(R.id.btnTypeTask);
                    selectPriorityChip(chipGroupPriority, task.getPriority());
                    switchImportant.setVisibility(View.GONE);
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            if (title.isEmpty()) {
                layoutTitle.setError("El título es obligatorio");
                return;
            }

            boolean hasTime = switchTimeBlock.isChecked();
            if (hasTime) {
                if (!startTime.isBefore(endTime)) {
                    Toast.makeText(getContext(), "La hora de inicio debe ser anterior a la de fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (viewModel.checkCollision(startTime, endTime, dateToUse, editingTaskId)) {
                    Toast.makeText(getContext(), "¡Atención! Este horario ya está ocupado", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean isEvent = toggleGroup.getCheckedButtonId() == R.id.btnTypeEvent;
            Priority priority = null;
            boolean isImportant = false;
            String color = null;

            if (!isEvent) {
                priority = getSelectedPriority(chipGroupPriority);
            } else {
                isImportant = switchImportant.isChecked();
                if (isImportant) {
                    color = getSelectedPaletteColor(chipGroupPalette);
                }
            }
            
            Task task;
            if (editingTaskId != null) {
                task = viewModel.getTaskById(editingTaskId);
            } else {
                task = new Task();
                task.setDeadline(LocalDateTime.of(dateToUse, LocalTime.MIDNIGHT));
            }

            if (task != null) {
                task.setTitle(title);
                task.setEvent(isEvent);
                task.setPriority(priority);
                task.setImportant(isImportant);
                task.setImportantColor(color);
                task.setHasTimeBlock(hasTime);
                if (hasTime) {
                    task.setStartTime(startTime);
                    task.setEndTime(endTime);
                } else {
                    task.setStartTime(null);
                    task.setEndTime(null);
                }
                
                task.setReminders(tempReminders);

                if (editingTaskId != null) {
                    viewModel.updateTask(task);
                } else {
                    viewModel.addTask(task);
                }
            }
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (editingTaskId != null) {
                viewModel.deleteTask(editingTaskId);
                dismiss();
            }
        });
    }

    private void updateRemindersButtonText(Button btn) {
        if (tempReminders.isEmpty()) {
            btn.setText("RECORDATORIOS");
        } else {
            btn.setText("RECORDATORIOS (" + tempReminders.size() + ")");
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
        
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                color,
                ContextCompat.getColor(requireContext(), R.color.white)
        };
        chip.setChipBackgroundColor(new ColorStateList(states, colors));
        chip.setChipStrokeColor(ColorStateList.valueOf(color));
        
        group.addView(chip);
        if (priority == Priority.LOW) chip.setChecked(true);
    }

    private void setupPaletteChips(ChipGroup group) {
        group.removeAllViews();
        addPaletteChip(group, "lavender", R.color.cat_lavender);
        addPaletteChip(group, "blue", R.color.cat_blue);
        addPaletteChip(group, "cyan", R.color.cat_cyan);
        addPaletteChip(group, "mint", R.color.cat_mint);
        addPaletteChip(group, "green", R.color.cat_green);
        addPaletteChip(group, "yellow", R.color.cat_yellow);
        addPaletteChip(group, "orange", R.color.cat_orange);
        addPaletteChip(group, "pink", R.color.cat_pink);
        addPaletteChip(group, "rose", R.color.cat_rose);
    }

    private void addPaletteChip(ChipGroup group, String colorName, int colorRes) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_priority_chip, group, false);
        chip.setText("");
        chip.setId(View.generateViewId());
        chip.setTag(colorName);
        chip.setChipIconVisible(false);
        chip.setCloseIconVisible(false);
        
        int color = ContextCompat.getColor(requireContext(), colorRes);
        
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                color,
                color // Siempre mostrar el color
        };
        chip.setChipBackgroundColor(new ColorStateList(states, colors));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.BLACK)); // Borde para resaltar seleccion
        chip.setChipStrokeWidth(0f);
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chip.setChipStrokeWidth(isChecked ? 4f : 0f);
        });

        group.addView(chip);
        if (colorName.equals("lavender")) chip.setChecked(true);
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

    private void selectPaletteChip(ChipGroup group, String colorName) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (colorName != null && colorName.equals(chip.getTag())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private Priority getSelectedPriority(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        Chip chip = group.findViewById(checkedId);
        if (chip != null) {
            return (Priority) chip.getTag();
        }
        return Priority.LOW;
    }

    private String getSelectedPaletteColor(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        Chip chip = group.findViewById(checkedId);
        if (chip != null) {
            return (String) chip.getTag();
        }
        return "lavender";
    }

    private void showTimePicker(boolean isStart, Button btn) {
        LocalTime current = isStart ? startTime : endTime;
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(current.getHour())
                .setMinute(current.getMinute())
                .setTitleText(isStart ? "Hora de inicio" : "Hora de fin")
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

        picker.show(getChildFragmentManager(), "time_picker");
    }
}
