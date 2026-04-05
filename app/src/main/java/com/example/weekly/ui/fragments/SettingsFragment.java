package com.example.weekly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.weekly.R;
import com.example.weekly.utils.DailyBriefingWorker;
import com.example.weekly.utils.SettingsManager;
import com.example.weekly.utils.TaskAlarmScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    @Inject
    SettingsManager settingsManager;

    @Inject
    TaskAlarmScheduler taskAlarmScheduler;

    private MaterialSwitch switchGlobalNotifications;
    private MaterialSwitch switchReminders;
    private MaterialSwitch switchImportantEvents;
    private TextView textBriefingTime;
    private TextView textCurrentTheme;
    
    private View layoutReminders;
    private View layoutImportantEvents;
    private View layoutBriefing;
    private View layoutAppTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchGlobalNotifications = view.findViewById(R.id.switchGlobalNotifications);
        switchReminders = view.findViewById(R.id.switchReminders);
        switchImportantEvents = view.findViewById(R.id.switchImportantEvents);
        textBriefingTime = view.findViewById(R.id.textBriefingTime);
        textCurrentTheme = view.findViewById(R.id.textCurrentTheme);
        
        layoutReminders = view.findViewById(R.id.layoutReminders);
        layoutImportantEvents = view.findViewById(R.id.layoutImportantEvents);
        layoutBriefing = view.findViewById(R.id.layoutBriefing);
        layoutAppTheme = view.findViewById(R.id.layoutAppTheme);

        setupSettings();

        return view;
    }

    private void setupSettings() {
        // Cargar valores iniciales
        boolean globalEnabled = settingsManager.areGlobalNotificationsEnabled();
        switchGlobalNotifications.setChecked(globalEnabled);
        switchReminders.setChecked(settingsManager.areRemindersEnabled());
        switchImportantEvents.setChecked(settingsManager.areImportantEventsEnabled());
        updateBriefingTimeText(settingsManager.getBriefingHour(), settingsManager.getBriefingMinute());
        updateThemeText(settingsManager.getAppTheme());
        
        updateSubSettingsVisibility(globalEnabled);

        // Listeners para guardar cambios
        switchGlobalNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setGlobalNotificationsEnabled(isChecked);
            updateSubSettingsVisibility(isChecked);
            if (!isChecked) {
                taskAlarmScheduler.cancelAllAlarms();
            }
        });

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setRemindersEnabled(isChecked);
        });

        switchImportantEvents.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setImportantEventsEnabled(isChecked);
        });

        textBriefingTime.setOnClickListener(v -> {
            showTimePicker();
        });

        layoutAppTheme.setOnClickListener(v -> {
            showThemeSelector();
        });
    }

    private void updateSubSettingsVisibility(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.4f;
        layoutReminders.setAlpha(alpha);
        layoutImportantEvents.setAlpha(alpha);
        layoutBriefing.setAlpha(alpha);
        
        switchReminders.setEnabled(enabled);
        switchImportantEvents.setEnabled(enabled);
        textBriefingTime.setEnabled(enabled);
    }

    private void showThemeSelector() {
        String[] options = {"Claro", "Oscuro", "Predeterminado del sistema"};
        int[] modes = {
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        };

        int currentMode = settingsManager.getAppTheme();
        int checkedItem = 2; // Default to System
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 0;
        else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar Tema")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int selectedMode = modes[which];
                    settingsManager.setAppTheme(selectedMode);
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    updateThemeText(selectedMode);
                    dialog.dismiss();
                })
                .show();
    }

    private void updateThemeText(int mode) {
        String text = "Sistema";
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) text = "Claro";
        else if (mode == AppCompatDelegate.MODE_NIGHT_YES) text = "Oscuro";
        textCurrentTheme.setText(text);
    }

    private void showTimePicker() {
        int hour = settingsManager.getBriefingHour();
        int minute = settingsManager.getBriefingMinute();

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Seleccionar hora del resumen")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int newHour = picker.getHour();
            int newMinute = picker.getMinute();
            settingsManager.setBriefingTime(newHour, newMinute);
            updateBriefingTimeText(newHour, newMinute);
            // Reprogramar el Worker con la nueva hora
            DailyBriefingWorker.schedule(requireContext());
        });

        picker.show(getChildFragmentManager(), "daily_briefing_time_picker");
    }

    private void updateBriefingTimeText(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
        textBriefingTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm));
    }
}
