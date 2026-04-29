package com.example.weekly.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.SpleetTask;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpleetTaskAdapter extends ListAdapter<SpleetTaskAdapter.SpleetItem, RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final OnSpleetInteractionListener listener;

    public interface OnSpleetInteractionListener {
        void onEdit(SpleetTask task);
        void onDelete(SpleetTask task);
    }

    public SpleetTaskAdapter(OnSpleetInteractionListener listener) {
        super(new SpleetDiffCallback());
        this.listener = listener;
    }

    public void setSpleetTasks(List<SpleetTask> tasks) {
        List<SpleetItem> items = new ArrayList<>();
        int lastDay = -1;
        
        if (tasks != null) {
            for (SpleetTask task : tasks) {
                if (task.dayOfWeek != lastDay) {
                    items.add(new SpleetItem(task.dayOfWeek));
                    lastDay = task.dayOfWeek;
                }
                items.add(new SpleetItem(task));
            }
        }
        submitList(items);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader ? TYPE_HEADER : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spleet_day_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SpleetItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item.dayOfWeek);
        } else {
            ((TaskViewHolder) holder).bind(item.task, listener);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }
        public void bind(int day) {
            String[] days = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            text.setText(days[day]);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, time, priorityLabel;
        View indicator, dragHandle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTaskTitle);
            time = itemView.findViewById(R.id.textTaskTime);
            priorityLabel = itemView.findViewById(R.id.textPriorityLabel);
            indicator = itemView.findViewById(R.id.priorityIndicator);
            dragHandle = itemView.findViewById(R.id.imgDragHandle);
            itemView.findViewById(R.id.checkCompleted).setVisibility(View.GONE);
        }

        public void bind(SpleetTask task, OnSpleetInteractionListener listener) {
            title.setText(task.title);
            
            if (dragHandle != null) {
                dragHandle.setVisibility(View.GONE);
            }

            if (task.hasTimeBlock && task.startTime != null && task.endTime != null) {
                time.setVisibility(View.VISIBLE);
                time.setText(task.startTime.format(TIME_FORMATTER) + " - " + task.endTime.format(TIME_FORMATTER));
            } else {
                time.setVisibility(View.GONE);
            }

            indicator.setVisibility(View.GONE);

            if (task.priority != null) {
                int color = getPriorityColor(task.priority, itemView);
                String priorityName = task.priority.name();
                
                priorityLabel.setVisibility(View.VISIBLE);
                priorityLabel.setBackgroundColor(color);
                priorityLabel.setText(priorityName);
            } else {
                priorityLabel.setVisibility(View.VISIBLE);
                if (task.isImportant) {
                    priorityLabel.setBackgroundColor(0xFFFFD700); // Dorado
                    priorityLabel.setText("EVENTO IMP.");
                } else {
                    priorityLabel.setBackgroundColor(0xFFBDBDBD); // Gris
                    priorityLabel.setText("EVENTO");
                }
            }

            itemView.setOnClickListener(v -> listener.onEdit(task));
            itemView.setOnLongClickListener(null);
        }

        private int getPriorityColor(Priority priority, View view) {
            int colorRes;
            switch (priority) {
                case HIGH: colorRes = R.color.priority_high; break;
                case MEDIUM: colorRes = R.color.priority_medium; break;
                default: colorRes = R.color.priority_low; break;
            }
            return ContextCompat.getColor(view.getContext(), colorRes);
        }
    }

    static class SpleetItem {
        boolean isHeader;
        int dayOfWeek;
        SpleetTask task;

        SpleetItem(int day) { this.isHeader = true; this.dayOfWeek = day; }
        SpleetItem(SpleetTask task) { this.isHeader = false; this.task = new SpleetTask(task); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SpleetItem that = (SpleetItem) o;
            if (isHeader != that.isHeader) return false;
            if (isHeader) return dayOfWeek == that.dayOfWeek;
            
            if (task == null || that.task == null) return task == that.task;
            return task.equals(that.task);
        }
        @Override
        public int hashCode() { return Objects.hash(isHeader, dayOfWeek, task); }
    }

    static class SpleetDiffCallback extends DiffUtil.ItemCallback<SpleetItem> {
        @Override
        public boolean areItemsTheSame(@NonNull SpleetItem old, @NonNull SpleetItem newI) {
            if (old.isHeader && newI.isHeader) return old.dayOfWeek == newI.dayOfWeek;
            if (!old.isHeader && !newI.isHeader) {
                if (old.task == null || newI.task == null) return false;
                return Objects.equals(old.task.id, newI.task.id);
            }
            return false;
        }
        @Override
        public boolean areContentsTheSame(@NonNull SpleetItem old, @NonNull SpleetItem newI) {
            return old.equals(newI);
        }
    }
}
