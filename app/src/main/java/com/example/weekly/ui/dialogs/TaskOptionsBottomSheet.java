package com.example.weekly.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weekly.R;
import com.example.weekly.domain.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TaskOptionsBottomSheet extends BottomSheetDialogFragment {

    private final Task task;
    private final OnTaskOptionListener listener;

    public interface OnTaskOptionListener {
        void onEdit(Task task);
        void onToggleStatus(Task task);
        void onDelete(Task task);
    }

    public TaskOptionsBottomSheet(Task task, OnTaskOptionListener listener) {
        this.task = task;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_task_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTitle = view.findViewById(R.id.textTaskTitle);
        Button btnComplete = view.findViewById(R.id.btnCompleteTask);
        Button btnEdit = view.findViewById(R.id.btnEditTask);
        Button btnDelete = view.findViewById(R.id.btnDeleteTask);

        textTitle.setText(task.getTitle());
        
        if (task.getPriority() == null) {
            btnComplete.setVisibility(View.GONE);
        } else {
            btnComplete.setText(task.isCompletada() ? "MARCAR COMO PENDIENTE" : "MARCAR COMO COMPLETADA");
            btnComplete.setOnClickListener(v -> {
                listener.onToggleStatus(task);
                dismiss();
            });
        }

        btnEdit.setOnClickListener(v -> {
            listener.onEdit(task);
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            listener.onDelete(task);
            dismiss();
        });
    }
}
