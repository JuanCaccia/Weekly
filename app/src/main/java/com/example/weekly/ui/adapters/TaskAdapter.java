package com.example.weekly.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private OnTaskInteractionListener listener;
    private boolean isRemainsMode = false;

    public interface OnTaskInteractionListener {
        void onTaskStatusChanged(Task task);
        void onTaskDelete(Task task);
        void onTaskArchive(Task task);
    }

    public TaskAdapter() {
        super(new TaskDiffCallback());
    }

    public void setOnTaskInteractionListener(OnTaskInteractionListener listener) {
        this.listener = listener;
    }

    public void setRemainsMode(boolean remainsMode) {
        this.isRemainsMode = remainsMode;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task, listener, isRemainsMode);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textTime, textPriorityLabel, textOverdue, textDayOfWeek;
        CheckBox checkBox;
        View priorityIndicator, timeContainer;
        ImageView imgCollisionAlert, imgBookmark, btnEditTask;
        com.google.android.material.card.MaterialCardView cardTask;
        View rescheduleBadge;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textTime = itemView.findViewById(R.id.textTaskTime);
            textPriorityLabel = itemView.findViewById(R.id.textPriorityLabel);
            textOverdue = itemView.findViewById(R.id.textOverdue);
            textDayOfWeek = itemView.findViewById(R.id.textDayOfWeek);
            checkBox = itemView.findViewById(R.id.checkCompleted);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            timeContainer = itemView.findViewById(R.id.timeContainer);
            imgCollisionAlert = itemView.findViewById(R.id.imgCollisionAlert);
            imgBookmark = itemView.findViewById(R.id.imgBookmark);
            rescheduleBadge = itemView.findViewById(R.id.rescheduleBadge);
            btnEditTask = itemView.findViewById(R.id.btnEditTask);
        }

        public void bind(Task task, OnTaskInteractionListener listener, boolean remainsMode) {
            textTitle.setText(task.getTitle());
            
            if (task.isCompletada()) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(0.6f);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                itemView.setAlpha(1.0f);
            }

            // Manejo de colisión visual
            if (task.hasCollision) {
                imgCollisionAlert.setVisibility(View.VISIBLE);
            } else {
                imgCollisionAlert.setVisibility(View.GONE);
            }

            // Reset de estilos de eventos importantes
            imgBookmark.setVisibility(View.GONE);
            
            // Revertir a colorSurface del tema
            TypedValue typedValue = new TypedValue();
            itemView.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
            int colorSurface = typedValue.data;
            cardTask.setCardBackgroundColor(colorSurface);

            ViewGroup.LayoutParams indicatorParams = priorityIndicator.getLayoutParams();
            indicatorParams.width = (int) (4 * itemView.getResources().getDisplayMetrics().density);
            priorityIndicator.setLayoutParams(indicatorParams);

            // Lógica de visualización para modo Remains
            if (remainsMode) {
                timeContainer.setVisibility(View.VISIBLE);
                btnEditTask.setVisibility(View.GONE);
                
                if (task.getDeadline() != null) {
                    LocalDate deadlineDate = task.getDeadline().toLocalDate();
                    
                    String dayName = deadlineDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
                    String dateStr = deadlineDate.format(DATE_FORMATTER);
                    
                    textDayOfWeek.setText(dayName + " " + dateStr);
                    textDayOfWeek.setVisibility(View.VISIBLE);

                    long daysPast = ChronoUnit.DAYS.between(deadlineDate, LocalDate.now());
                    if (daysPast > 0) {
                        textOverdue.setVisibility(View.VISIBLE);
                        textOverdue.setText(daysPast == 1 ? "Venció ayer" : "Hace " + daysPast + " días");
                    } else {
                        textOverdue.setVisibility(View.GONE);
                    }
                } else {
                    textDayOfWeek.setVisibility(View.GONE);
                    textOverdue.setVisibility(View.VISIBLE);
                    textOverdue.setText("Sin fecha");
                }
                
                if (rescheduleBadge != null) {
                    rescheduleBadge.setVisibility(View.GONE);
                }
                
                if (!task.isHasTimeBlock()) {
                    textTime.setVisibility(View.GONE);
                } else {
                    textTime.setVisibility(View.VISIBLE);
                    if (task.getStartTime() != null && task.getEndTime() != null) {
                        String timeText = task.getStartTime().format(TIME_FORMATTER) + " - " + task.getEndTime().format(TIME_FORMATTER);
                        textTime.setText(timeText);
                    }
                }
            } else {
                btnEditTask.setVisibility(View.VISIBLE);
                textDayOfWeek.setVisibility(View.GONE);
                textOverdue.setVisibility(View.GONE);
                
                if (task.isHasTimeBlock() && task.getStartTime() != null && task.getEndTime() != null) {
                    String timeText = task.getStartTime().format(TIME_FORMATTER) + " - " + task.getEndTime().format(TIME_FORMATTER);
                    textTime.setText(timeText);
                    textTime.setVisibility(View.VISIBLE);
                    timeContainer.setVisibility(View.VISIBLE);
                } else {
                    timeContainer.setVisibility(View.GONE);
                }
            }

            priorityIndicator.setVisibility(View.GONE);

            if (task.getPriority() == null) { 
                checkBox.setVisibility(View.GONE);
                textPriorityLabel.setVisibility(View.GONE);
                
                // Si es un evento importante, aplicar estilos
                if (task.isImportant()) {
                    imgBookmark.setVisibility(View.VISIBLE);
                    int color = getImportantColor(task.getImportantColor());
                    
                    // Fondo con 10% opacidad
                    int alphaColor = Color.argb(25, Color.red(color), Color.green(color), Color.blue(color));
                    cardTask.setCardBackgroundColor(alphaColor);
                    
                    // Borde lateral grueso
                    priorityIndicator.setVisibility(View.VISIBLE);
                    priorityIndicator.setBackgroundColor(color);
                    indicatorParams.width = (int) (8 * itemView.getResources().getDisplayMetrics().density);
                    priorityIndicator.setLayoutParams(indicatorParams);
                    
                    imgBookmark.setImageTintList(ColorStateList.valueOf(color));
                }
            } else {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(task.isCompletada());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (task.isCompletada() != isChecked) {
                        task.setCompletada(isChecked);
                        if (listener != null) {
                            listener.onTaskStatusChanged(task);
                        }
                    }
                });

                int color = getPriorityColor(task.getPriority());
                String priorityName = task.getPriority().name();
                
                textPriorityLabel.setText(priorityName);
                textPriorityLabel.setBackgroundColor(color);
                textPriorityLabel.setVisibility(View.VISIBLE);
            }

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDelete(task);
                }
                return true;
            });
        }

        private int getPriorityColor(Priority priority) {
            int colorRes;
            switch (priority) {
                case HIGH: colorRes = R.color.priority_high; break;
                case MEDIUM: colorRes = R.color.priority_medium; break;
                default: colorRes = R.color.priority_low; break;
            }
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }

        private int getImportantColor(String colorName) {
            if (colorName == null) return 0xFFBFA8E6; // Violeta por defecto
            
            switch (colorName) {
                case "lavender": return ContextCompat.getColor(itemView.getContext(), R.color.cat_lavender);
                case "blue": return ContextCompat.getColor(itemView.getContext(), R.color.cat_blue);
                case "cyan": return ContextCompat.getColor(itemView.getContext(), R.color.cat_cyan);
                case "mint": return ContextCompat.getColor(itemView.getContext(), R.color.cat_mint);
                case "green": return ContextCompat.getColor(itemView.getContext(), R.color.cat_green);
                case "yellow": return ContextCompat.getColor(itemView.getContext(), R.color.cat_yellow);
                case "orange": return ContextCompat.getColor(itemView.getContext(), R.color.cat_orange);
                case "pink": return ContextCompat.getColor(itemView.getContext(), R.color.cat_pink);
                case "rose": return ContextCompat.getColor(itemView.getContext(), R.color.cat_rose);
                default: return 0xFFBFA8E6;
            }
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
