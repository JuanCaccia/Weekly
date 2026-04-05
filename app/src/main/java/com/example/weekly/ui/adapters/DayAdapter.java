package com.example.weekly.ui.adapters;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.HeatmapService;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.Task;
import com.example.weekly.ui.models.DayPlan;
import com.example.weekly.ui.utils.TimelineMapper;
import com.example.weekly.ui.views.TimelineBackgroundView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DayAdapter extends ListAdapter<DayPlan, RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_REMAINS = 1;
    private static final long CLICK_THRESHOLD = 300;
    private long lastClickTime = 0;
    private RecyclerView recyclerView;

    private final OnDayPlanInteractionListener listener;

    public interface OnDayPlanInteractionListener {
        void onToggleExpansion(LocalDate date);
        void onTaskStatusChanged(Task task, boolean completed);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onTaskArchive(Task task);
        void onRescheduleAllRemains();
        void onArchiveAllRemains();
    }

    public DayAdapter(OnDayPlanInteractionListener listener) {
        super(new DayPlanDiffCallback());
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    private boolean canHandleClick() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < CLICK_THRESHOLD) return false;
        lastClickTime = now;
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType() == DayPlan.Type.REMAINS_HEADER ? TYPE_REMAINS : TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_REMAINS) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remains_header, parent, false);
            return new RemainsViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DayPlan plan = getItem(position);
        if (holder instanceof RemainsViewHolder) {
            ((RemainsViewHolder) holder).bind(plan, listener, this::applyExpansionTransition);
        } else {
            ((DayViewHolder) holder).bind(plan, listener, this::canHandleClick, this::applyExpansionTransition);
        }
    }

    private void applyExpansionTransition() {
        if (recyclerView == null) return;
        
        TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setPathMotion(null); 
        
        set.addTransition(changeBounds);
        set.addTransition(new Fade(Fade.IN).setDuration(150)); 
        
        set.setDuration(300);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        
        TransitionManager.beginDelayedTransition(recyclerView, set);
    }

    static class RemainsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSubtitle, textCount;
        private final View remainsContent, headerTrigger;
        private final ImageView chevron;
        private final RecyclerView rvRemains;
        private final Button btnRescheduleAll, btnArchiveAll;
        private final TaskAdapter taskAdapter;

        public RemainsViewHolder(@NonNull View itemView) {
            super(itemView);
            textSubtitle = itemView.findViewById(R.id.textRemainsSubtitle);
            textCount = itemView.findViewById(R.id.textRemainsCount);
            remainsContent = itemView.findViewById(R.id.remainsContent);
            headerTrigger = itemView.findViewById(R.id.remainsHeaderTrigger);
            chevron = itemView.findViewById(R.id.imageChevron);
            rvRemains = itemView.findViewById(R.id.rvRemains);
            btnRescheduleAll = itemView.findViewById(R.id.btnRescheduleAll);
            btnArchiveAll = itemView.findViewById(R.id.btnArchiveAll);

            taskAdapter = new TaskAdapter();
            taskAdapter.setRemainsMode(true);
            rvRemains.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvRemains.setAdapter(taskAdapter);
            
            if (rvRemains.getItemAnimator() != null) {
                rvRemains.getItemAnimator().setAddDuration(0);
                rvRemains.getItemAnimator().setChangeDuration(0);
                rvRemains.getItemAnimator().setMoveDuration(0);
                rvRemains.getItemAnimator().setRemoveDuration(0);
            }
        }

        public void bind(DayPlan plan, OnDayPlanInteractionListener listener, Runnable transitionApplier) {
            int count = plan.getTasks().size();
            textCount.setText(String.valueOf(count));
            if (textSubtitle != null) {
                textSubtitle.setText("Tienes " + count + " " + (count == 1 ? "tarea pendiente" : "tareas pendientes") + " de días anteriores");
            }
            
            boolean isExpanded = plan.isExpanded();
            // Actualizar contenido ANTES de cambiar visibilidad para anclaje estable
            taskAdapter.submitList(plan.getTasks());
            
            remainsContent.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            chevron.animate().rotation(isExpanded ? 180 : 0).setDuration(250).start();

            headerTrigger.setOnClickListener(v -> {
                transitionApplier.run();
                listener.onToggleExpansion(plan.getDate());
            });
            btnRescheduleAll.setOnClickListener(v -> listener.onRescheduleAllRemains());
            btnArchiveAll.setOnClickListener(v -> listener.onArchiveAllRemains());
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDayName, textDate, textNoActivities;
        private final View expandableLayout, dayHeader, layoutTimeline, timelineWrapper;
        private final LinearLayout importantIndicatorsContainer;
        private final ImageView chevron;
        private final ChipGroup chipGroupAllDay;
        private final ConstraintLayout timelineContainer;
        private final TimelineBackgroundView timelineBackground;
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayName = itemView.findViewById(R.id.textDayName);
            textDate = itemView.findViewById(R.id.textDayDate);
            textNoActivities = itemView.findViewById(R.id.textNoActivities);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            dayHeader = itemView.findViewById(R.id.dayHeader);
            layoutTimeline = itemView.findViewById(R.id.layoutTimeline);
            timelineWrapper = itemView.findViewById(R.id.timelineWrapper);
            chevron = itemView.findViewById(R.id.imageChevron);
            chipGroupAllDay = itemView.findViewById(R.id.chipGroupAllDayTasks);
            timelineContainer = itemView.findViewById(R.id.timelineContainer);
            timelineBackground = itemView.findViewById(R.id.timelineBackground);
            importantIndicatorsContainer = itemView.findViewById(R.id.importantIndicatorsContainer);
        }

        public void bind(DayPlan plan, OnDayPlanInteractionListener listener, ClickGuard guard, Runnable transitionApplier) {
            textDayName.setText(plan.getDayDisplayName());
            textDate.setText(plan.getDate().format(DateTimeFormatter.ofPattern("d/M")));
            
            int densityColor = getDensityColor(plan.getDensityLevel());
            dayHeader.setBackgroundColor(densityColor);

            boolean isExpanded = plan.isExpanded();
            chevron.animate().rotation(isExpanded ? 180 : 0).setDuration(250).start();

            importantIndicatorsContainer.removeAllViews();
            List<Task> importantEvents = plan.getTasks().stream()
                    .filter(Task::isImportant)
                    .collect(Collectors.toList());

            if (!importantEvents.isEmpty()) {
                importantIndicatorsContainer.setVisibility(View.VISIBLE);
                for (Task event : importantEvents) {
                    addImportantCircle(event.getImportantColor());
                }
            } else {
                importantIndicatorsContainer.setVisibility(View.GONE);
            }

            // Renderizar contenido ANTES de mostrar el layout expandible para asegurar anclaje
            renderTimeline(plan, listener, guard);
            expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            dayHeader.setOnClickListener(v -> {
                if (guard.canClick()) {
                    transitionApplier.run();
                    listener.onToggleExpansion(plan.getDate());
                }
            });
        }

        private void addImportantCircle(String colorName) {
            View circle = new View(itemView.getContext());
            int size = (int) (14 * itemView.getResources().getDisplayMetrics().density); 
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins((int) (4 * itemView.getResources().getDisplayMetrics().density), 0, 0, 0);
            circle.setLayoutParams(params);
            circle.setBackgroundResource(R.drawable.bg_circle);
            int color = getImportantColor(colorName, itemView);
            circle.setBackgroundTintList(ColorStateList.valueOf(color));
            importantIndicatorsContainer.addView(circle);
        }

        private void renderTimeline(DayPlan plan, OnDayPlanInteractionListener listener, ClickGuard guard) {
            timelineContainer.removeAllViews();
            chipGroupAllDay.removeAllViews();

            List<Task> timedTasks = new ArrayList<>();
            int minHourFound = 24;
            int maxHourFound = 0;
            boolean hasAnyTask = false;

            for (Task task : plan.getTasks()) {
                hasAnyTask = true;
                if (task.isHasTimeBlock() && task.getStartTime() != null && task.getEndTime() != null) {
                    timedTasks.add(task);
                    minHourFound = Math.min(minHourFound, task.getStartTime().getHour());
                    maxHourFound = Math.max(maxHourFound, task.getEndTime().getHour());
                } else {
                    addAllDayChip(task, listener, guard);
                }
            }

            if (!hasAnyTask) {
                textNoActivities.setVisibility(View.VISIBLE);
                textNoActivities.setText("Ninguna Actividad Para el Dia " + plan.getDayDisplayName());
                if (timelineWrapper != null) timelineWrapper.setVisibility(View.GONE);
                layoutTimeline.setVisibility(View.GONE); 
            } else {
                textNoActivities.setVisibility(View.GONE);
                if (timedTasks.isEmpty()) {
                    if (timelineWrapper != null) timelineWrapper.setVisibility(View.GONE);
                    layoutTimeline.setVisibility(View.GONE);
                } else {
                    layoutTimeline.setVisibility(View.VISIBLE);
                    if (timelineWrapper != null) timelineWrapper.setVisibility(View.VISIBLE);
                    int startHour = Math.max(0, minHourFound - 1);
                    int endHour = Math.min(24, maxHourFound + 2);
                    timelineBackground.setTimeRange(startHour, endHour);
                    // Pasar la fecha para el indicador de ahora
                    timelineBackground.setDate(plan.getDate());
                    for (Task task : timedTasks) {
                        addTimelineBlock(task, startHour, listener, guard);
                    }
                }
            }
        }

        private void addTimelineBlock(Task task, int startHour, OnDayPlanInteractionListener listener, ClickGuard guard) {
            MaterialCardView card = (MaterialCardView) LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_timeline_block, timelineContainer, false);
            TextView title = card.findViewById(R.id.textTaskTitle);
            TextView time = card.findViewById(R.id.textTaskTime);
            CheckBox check = card.findViewById(R.id.checkCompleted);
            View indicator = card.findViewById(R.id.priorityIndicator);

            title.setText(task.getTitle());
            time.setText(String.format("%s - %s", task.getStartTime().format(timeFormatter), task.getEndTime().format(timeFormatter)));
            
            boolean isDarkMode = (itemView.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            int emptyDayColor = isDarkMode ? 0xFF1C1A24 : Color.WHITE;
            card.setCardBackgroundColor(ColorStateList.valueOf(emptyDayColor));
            
            int strokeColor = isDarkMode ? 0xFF666666 : 0xFFCCCCCC;
            card.setStrokeColor(ColorStateList.valueOf(strokeColor));
            card.setStrokeWidth((int) (1.5f * itemView.getResources().getDisplayMetrics().density));

            if (task.isImportant()) {
                int color = getImportantColor(task.getImportantColor(), itemView);
                indicator.setBackgroundColor(color);
                title.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
                time.setTextColor(isDarkMode ? 0xFFCAC4D0 : 0xFF666666);
                card.setStrokeColor(ColorStateList.valueOf(color));
                card.setStrokeWidth((int) (2.5f * itemView.getResources().getDisplayMetrics().density));
            } else {
                applyPriorityStyle(indicator, task);
                if (isDarkMode) {
                    title.setTextColor(Color.WHITE);
                    time.setTextColor(0xFFCAC4D0);
                    check.setButtonTintList(ColorStateList.valueOf(Color.WHITE));
                } else {
                    title.setTextColor(Color.BLACK);
                    time.setTextColor(0xFF666666);
                    check.setButtonTintList(null);
                }
            }

            if (task.getPriority() == null) { 
                check.setVisibility(View.GONE);
            } else {
                check.setVisibility(View.VISIBLE);
                check.setOnCheckedChangeListener(null);
                check.setChecked(task.isCompletada());
                applyTaskVisualState(title, card, task.isCompletada());
                check.setOnClickListener(v -> {
                    boolean isChecked = ((CheckBox)v).isChecked();
                    applyTaskVisualState(title, card, isChecked);
                    listener.onTaskStatusChanged(task, isChecked);
                });
            }

            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, 0);
            lp.startToStart = R.id.timelineContainer;
            lp.endToEnd = R.id.timelineContainer;
            lp.topToTop = R.id.timelineContainer;
            lp.topMargin = TimelineMapper.getTopMarginPx(task.getStartTime(), startHour, itemView.getContext());
            lp.height = TimelineMapper.getHeightPx(task.getStartTime(), task.getEndTime(), itemView.getContext());
            card.setLayoutParams(lp);
            timelineContainer.addView(card);
            card.setOnClickListener(v -> { if (guard.canClick()) listener.onTaskEdit(task); });
        }

        private void addAllDayChip(Task task, OnDayPlanInteractionListener listener, ClickGuard guard) {
            Chip chip = new Chip(itemView.getContext());
            chip.setText(task.getTitle());
            chip.setCheckable(false);
            boolean isDarkMode = (itemView.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (task.isImportant()) {
                int color = getImportantColor(task.getImportantColor(), itemView);
                chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                hsv[2] *= 0.7f; 
                int darkColor = Color.HSVToColor(hsv);
                chip.setChipStrokeColor(ColorStateList.valueOf(darkColor));
                chip.setChipStrokeWidth(3f);
                chip.setTextColor(androidx.core.graphics.ColorUtils.calculateLuminance(color) > 0.5 ? Color.BLACK : Color.WHITE);
            } else {
                chip.setChipStrokeWidth(1f);
                chip.setChipStrokeColor(ColorStateList.valueOf(isDarkMode ? 0xFF333333 : 0xFFE0E0E0));
                if (task.getPriority() == null) {
                    int emptyDayColor = isDarkMode ? 0xFF1C1A24 : Color.WHITE;
                    chip.setChipBackgroundColor(ColorStateList.valueOf(emptyDayColor));
                    chip.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
                } else {
                    int color = getPriorityColor(task.getPriority(), itemView);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                    chip.setTextColor(Color.WHITE);
                    if (task.isCompletada()) {
                        chip.setPaintFlags(chip.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        chip.setAlpha(0.6f);
                    }
                }
            }
            chip.setOnClickListener(v -> { if (guard.canClick()) listener.onTaskEdit(task); });
            chipGroupAllDay.addView(chip);
        }

        private void applyPriorityStyle(View indicator, Task task) {
            if (task.getPriority() == null) {
                indicator.setBackgroundColor(0xFFE0E0E0);
            } else {
                indicator.setBackgroundColor(getPriorityColor(task.getPriority(), itemView));
            }
        }

        private void applyTaskVisualState(TextView title, View block, boolean isChecked) {
            if (isChecked) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                block.setAlpha(0.6f);
            } else {
                title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                block.setAlpha(1.0f);
            }
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

        private int getImportantColor(String colorName, View view) {
            if (colorName == null) return 0xFFBFA8E6;
            switch (colorName) {
                case "lavender": return ContextCompat.getColor(view.getContext(), R.color.cat_lavender);
                case "blue": return ContextCompat.getColor(view.getContext(), R.color.cat_blue);
                case "cyan": return ContextCompat.getColor(view.getContext(), R.color.cat_cyan);
                case "mint": return ContextCompat.getColor(view.getContext(), R.color.cat_mint);
                case "green": return ContextCompat.getColor(view.getContext(), R.color.cat_green);
                case "yellow": return ContextCompat.getColor(view.getContext(), R.color.cat_yellow);
                case "orange": return ContextCompat.getColor(view.getContext(), R.color.cat_orange);
                case "pink": return ContextCompat.getColor(view.getContext(), R.color.cat_pink);
                case "rose": return ContextCompat.getColor(view.getContext(), R.color.cat_rose);
                default: return 0xFFBFA8E6;
            }
        }

        private int getDensityColor(HeatmapService.DensityLevel level) {
            int colorRes;
            switch (level) {
                case BAJA: colorRes = R.color.priority_low; break;
                case MEDIA: colorRes = R.color.priority_medium; break;
                case ALTA: colorRes = R.color.priority_high; break;
                case NULA:
                default: 
                    boolean isDarkMode = (itemView.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                    if (isDarkMode) return 0xFF1C1A24; 
                    return Color.WHITE;
            }
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }
    }

    private static class DayPlanDiffCallback extends DiffUtil.ItemCallback<DayPlan> {
        @Override
        public boolean areItemsTheSame(@NonNull DayPlan oldItem, @NonNull DayPlan newItem) {
            return Objects.equals(oldItem.getDate(), newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DayPlan oldItem, @NonNull DayPlan newItem) {
            return oldItem.equals(newItem);
        }
    }

    private interface ClickGuard {
        boolean canClick();
    }
}
