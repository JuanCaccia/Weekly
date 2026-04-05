package com.example.weekly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.example.weekly.ui.adapters.TaskAdapter;
import com.example.weekly.ui.viewmodels.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RemainsFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {
    private MainViewModel viewModel;
    private TaskAdapter adapter;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remains, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        RecyclerView rv = view.findViewById(R.id.rvRemains);
        layoutEmpty = view.findViewById(R.id.layoutEmptyRemains);
        
        adapter = new TaskAdapter();
        adapter.setRemainsMode(true);
        adapter.setOnTaskInteractionListener(this);
        
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        viewModel.remains.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.submitList(tasks);
                layoutEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        viewModel.saveTask(task);
    }

    @Override
    public void onTaskDelete(Task task) {
        viewModel.deleteTask(task);
    }

    @Override
    public void onTaskArchive(Task task) {
    }
}
