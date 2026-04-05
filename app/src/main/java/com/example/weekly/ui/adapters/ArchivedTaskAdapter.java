package com.example.weekly.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Task;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ArchivedTaskAdapter extends ListAdapter<Task, ArchivedTaskAdapter.ArchivedTaskViewHolder> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final OnTaskRestoreListener listener;

    public interface OnTaskRestoreListener {
        void onTaskRestore(Task task);
    }

    public ArchivedTaskAdapter(OnTaskRestoreListener listener) {
        super(new TaskDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArchivedTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_archived_task, parent, false);
        return new ArchivedTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivedTaskViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ArchivedTaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDate, textArchivedDate;
        Button btnRestore;

        public ArchivedTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textDate = itemView.findViewById(R.id.textTaskDate);
            textArchivedDate = itemView.findViewById(R.id.textArchivedDate);
            btnRestore = itemView.findViewById(R.id.btnRestore);
        }

        public void bind(Task task, OnTaskRestoreListener listener) {
            textTitle.setText(task.getTitle());
            
            if (task.getDeadline() != null) {
                textDate.setText("Para el " + task.getDeadline().format(DATE_FORMATTER));
                textDate.setVisibility(View.VISIBLE);
            } else {
                textDate.setVisibility(View.GONE);
            }

            if (task.getArchivedAt() != null) {
                textArchivedDate.setText("Archivado el " + task.getArchivedAt().format(DATE_FORMATTER));
                textArchivedDate.setVisibility(View.VISIBLE);
            } else {
                textArchivedDate.setVisibility(View.GONE);
            }

            btnRestore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskRestore(task);
                }
            });
        }
    }

    private static class TaskDiffCallback extends DiffUtil.ItemCallback<Task> {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return Objects.equals(oldItem.id, newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.equals(newItem);
        }
    }
}
