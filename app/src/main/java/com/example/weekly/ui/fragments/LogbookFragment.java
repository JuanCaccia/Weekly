package com.example.weekly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.example.weekly.ui.adapters.ArchivedTaskAdapter;
import com.example.weekly.ui.viewmodels.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LogbookFragment extends DialogFragment implements ArchivedTaskAdapter.OnTaskRestoreListener {

    private MainViewModel viewModel;
    private ArchivedTaskAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logbook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        RecyclerView rv = view.findViewById(R.id.rvArchivedTasks);
        adapter = new ArchivedTaskAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        viewModel.archivedTasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.submitList(tasks);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onTaskRestore(Task task) {
        viewModel.unarchiveTask(task);
        Toast.makeText(getContext(), "Tarea restaurada", Toast.LENGTH_SHORT).show();
    }
}
