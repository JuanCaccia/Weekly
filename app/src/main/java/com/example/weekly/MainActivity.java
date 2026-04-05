package com.example.weekly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weekly.ui.fragments.ProgressFragment;
import com.example.weekly.ui.fragments.RemainsFragment;
import com.example.weekly.ui.fragments.SettingsFragment;
import com.example.weekly.ui.fragments.SpleetFragment;
import com.example.weekly.ui.fragments.WeekFragment;
import com.example.weekly.ui.viewmodels.MainViewModel;
import com.example.weekly.utils.SettingsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    
    @Inject
    SettingsManager settingsManager;
    
    private MainViewModel viewModel;
    private BottomNavigationView navView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Las notificaciones están desactivadas. No recibirás recordatorios.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Aplicar el tema guardado (settingsManager ya está inyectado aquí)
        int savedTheme = settingsManager.getAppTheme();
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        navView = findViewById(R.id.bottom_navigation);
        navView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Fragmento inicial (Semana)
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }

        checkNotificationPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        if (intent.getBooleanExtra("ACTION_NAVIGATE_TO_TASK", false)) {
            long baseTimeMillis = intent.getLongExtra("BASE_TIME_MILLIS", 0);
            if (baseTimeMillis > 0) {
                LocalDate date = Instant.ofEpochMilli(baseTimeMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                navView.setSelectedItemId(R.id.nav_week);
                viewModel.navigateToDate(date);
            }
        } else if (intent.getBooleanExtra("ACTION_SCROLL_TO_TODAY", false)) {
            navView.setSelectedItemId(R.id.nav_week);
            viewModel.currentWeek();
            viewModel.toggleDayExpansion(LocalDate.now());
        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            navView.setSelectedItemId(R.id.nav_week);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_week) {
            fragment = new WeekFragment();
        } else if (id == R.id.nav_remains) {
            fragment = new RemainsFragment();
        } else if (id == R.id.nav_spleet) {
            fragment = new SpleetFragment();
        } else if (id == R.id.nav_progress) {
            fragment = new ProgressFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
