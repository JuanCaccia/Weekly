package com.example.weekly.ui.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.weekly.R;
import com.example.weekly.domain.TaskReminder;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class RemindersDialogFragment extends DialogFragment {

    public interface OnRemindersSelectedListener {
        void onRemindersSelected(List<TaskReminder> reminders);
    }

    private OnRemindersSelectedListener listener;
    private List<TaskReminder> currentReminders = new ArrayList<>();
    private ChipGroup chipGroupReminders;
    private boolean hasTimeBlock = true;
    private LocalDate defaultDate;
    private boolean isFirstStart = true;

    public static RemindersDialogFragment newInstance(List<TaskReminder> reminders) {
        return newInstance(reminders, true, LocalDate.now());
    }

    public static RemindersDialogFragment newInstance(List<TaskReminder> reminders, boolean hasTimeBlock, LocalDate defaultDate) {
        RemindersDialogFragment fragment = new RemindersDialogFragment();
        fragment.currentReminders = new ArrayList<>(reminders);
        fragment.hasTimeBlock = hasTimeBlock;
        fragment.defaultDate = defaultDate;
        return fragment;
    }

    public void setOnRemindersSelectedListener(OnRemindersSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_reminders_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupReminders = view.findViewById(R.id.chipGroupReminders);
        
        View relativeHeader = view.findViewById(R.id.textRelativeHeader);
        View relativeOptions = view.findViewById(R.id.chipGroupAddOptions);
        TextView specificHeader = view.findViewById(R.id.textSpecificHeader);
        Chip chipAddSpecific = view.findViewById(R.id.chipAddSpecific);
        
        if (!hasTimeBlock) {
            if (relativeHeader != null) relativeHeader.setVisibility(View.GONE);
            if (relativeOptions != null) relativeOptions.setVisibility(View.GONE);
            
            if (specificHeader != null) specificHeader.setText("Añadir aviso");
            if (chipAddSpecific != null) {
                chipAddSpecific.setText("Seleccionar hora...");
            }
            
            // Si no hay recordatorios y es la primera vez que se abre, mostrar el reloj directamente
            if (currentReminders.isEmpty() && isFirstStart) {
                isFirstStart = false;
                showMaterialTimePicker(defaultDate != null ? defaultDate : LocalDate.now());
            }
        }

        view.findViewById(R.id.chipAdd5m).setOnClickListener(v -> addRelativeReminder(5));
        view.findViewById(R.id.chipAdd15m).setOnClickListener(v -> addRelativeReminder(15));
        view.findViewById(R.id.chipAdd1h).setOnClickListener(v -> addRelativeReminder(60));
        
        chipAddSpecific.setOnClickListener(v -> {
            if (hasTimeBlock) {
                showMaterialDatePicker();
            } else {
                showMaterialTimePicker(defaultDate != null ? defaultDate : LocalDate.now());
            }
        });

        view.findViewById(R.id.btnDoneReminders).setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemindersSelected(currentReminders);
            }
            dismiss();
        });

        refreshRemindersUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void addRelativeReminder(int minutes) {
        for (TaskReminder r : currentReminders) {
            if (r.minutesBefore != null && r.minutesBefore == minutes) return;
        }
        TaskReminder reminder = new TaskReminder();
        reminder.minutesBefore = minutes;
        currentReminders.add(reminder);
        refreshRemindersUI();
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            showMaterialTimePicker(selectedDate);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void showMaterialTimePicker(LocalDate date) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(LocalTime.now().getHour())
                .setMinute(LocalTime.now().getMinute())
                .setTitleText("Seleccionar hora")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            LocalTime selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            addSpecificReminder(date, selectedTime);
        });

        timePicker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void addSpecificReminder(LocalDate date, LocalTime time) {
        TaskReminder reminder = new TaskReminder();
        reminder.specificDate = date.toString();
        reminder.specificTime = time.toString();
        
        for (TaskReminder r : currentReminders) {
            if (reminder.equals(r)) return;
        }
        
        currentReminders.add(reminder);
        refreshRemindersUI();
    }

    private void refreshRemindersUI() {
        chipGroupReminders.removeAllViews();
        for (TaskReminder reminder : currentReminders) {
            Chip chip = new Chip(requireContext());
            chip.setText(formatReminder(reminder));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                currentReminders.remove(reminder);
                refreshRemindersUI();
            });
            chipGroupReminders.addView(chip);
        }
    }

    private String formatReminder(TaskReminder reminder) {
        if (reminder.minutesBefore != null) {
            int minutes = reminder.minutesBefore;
            if (minutes < 60) return minutes + " min antes";
            int hours = minutes / 60;
            return hours + (hours == 1 ? " hora antes" : " horas antes");
        } else if (reminder.specificTime != null) {
            String time = reminder.specificTime;
            String date = reminder.specificDate != null ? reminder.specificDate : "";
            return "El " + date + " a las " + time;
        }
        return "Recordatorio";
    }
}
