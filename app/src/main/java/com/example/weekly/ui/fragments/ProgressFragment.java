package com.example.weekly.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weekly.R;
import com.example.weekly.domain.Priority;
import com.example.weekly.domain.Task;
import com.example.weekly.ui.viewmodels.MainViewModel;
import com.example.weekly.ui.views.MultiColorCircleView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProgressFragment extends Fragment {
    private MainViewModel viewModel;
    private ViewPager2 viewPagerCalendar;
    private CalendarMonthPagerAdapter pagerAdapter;
    private ImportantEventsAdapter eventsAdapter;
    private TextView textMonthName;
    private final YearMonth baseMonth = YearMonth.now();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        textMonthName = view.findViewById(R.id.textMonthName);
        viewPagerCalendar = view.findViewById(R.id.viewPagerCalendar);
        
        pagerAdapter = new CalendarMonthPagerAdapter(date -> {
            viewModel.navigateToDate(date);
            BottomNavigationView navView = requireActivity().findViewById(R.id.bottom_navigation);
            if (navView != null) {
                navView.setSelectedItemId(R.id.nav_week);
            }
        });
        
        viewPagerCalendar.setAdapter(pagerAdapter);
        viewPagerCalendar.setCurrentItem(500, false);
        
        updateMonthName(500);

        viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMonthName(position);
                updateEventsList(position);
            }
        });

        RecyclerView rvEvents = view.findViewById(R.id.rvImportantEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new ImportantEventsAdapter();
        rvEvents.setAdapter(eventsAdapter);

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> 
            viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() - 1));
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> 
            viewPagerCalendar.setCurrentItem(viewPagerCalendar.getCurrentItem() + 1));

        viewModel.findAllLive().observe(getViewLifecycleOwner(), tasks -> {
            pagerAdapter.setTasks(tasks);
            updateEventsList(viewPagerCalendar.getCurrentItem());
        });
    }

    private void updateMonthName(int position) {
        YearMonth month = baseMonth.plusMonths(position - 500);
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        textMonthName.setText(monthName + " " + month.getYear());
    }

    private void updateEventsList(int position) {
        YearMonth month = baseMonth.plusMonths(position - 500);
        List<Task> tasks = pagerAdapter.getTasks();
        if (tasks == null) return;
        
        List<Task> monthEvents = tasks.stream()
                .filter(t -> t.isImportant() && t.getDeadline() != null && 
                            YearMonth.from(t.getDeadline().toLocalDate()).equals(month))
                .sorted(Comparator.comparing(Task::getDeadline))
                .collect(Collectors.toList());
        
        eventsAdapter.setEvents(monthEvents);
        
        View emptyLayout = getView().findViewById(R.id.layoutNoImportantEvents);
        if (emptyLayout != null) {
            emptyLayout.setVisibility(monthEvents.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private static class CalendarMonthPagerAdapter extends RecyclerView.Adapter<CalendarMonthPagerAdapter.MonthViewHolder> {
        private final List<Task> tasks = new ArrayList<>();
        private final OnDateClickListener listener;
        private final YearMonth baseMonth = YearMonth.now();

        public CalendarMonthPagerAdapter(OnDateClickListener listener) {
            this.listener = listener;
        }

        public void setTasks(List<Task> newTasks) {
            tasks.clear();
            tasks.addAll(newTasks);
            notifyDataSetChanged();
        }

        public List<Task> getTasks() {
            return tasks;
        }

        @NonNull
        @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView rv = new RecyclerView(parent.getContext());
            rv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rv.setLayoutManager(new GridLayoutManager(parent.getContext(), 7));
            rv.setNestedScrollingEnabled(false);
            return new MonthViewHolder(rv);
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
            YearMonth month = baseMonth.plusMonths(position - 500);
            CalendarMonthAdapter adapter = new CalendarMonthAdapter(date -> {
                if (listener != null) listener.onDateClick(date);
            });
            holder.recyclerView.setAdapter(adapter);
            adapter.setData(month, tasks);
        }

        @Override
        public int getItemCount() {
            return 1000;
        }

        static class MonthViewHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;
            MonthViewHolder(@NonNull View itemView) {
                super(itemView);
                recyclerView = (RecyclerView) itemView;
            }
        }
    }

    public interface OnDateClickListener {
        void onDateClick(LocalDate date);
    }

    private static class ImportantEventsAdapter extends RecyclerView.Adapter<ImportantEventsAdapter.ViewHolder> {
        private final List<Task> events = new ArrayList<>();
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public void setEvents(List<Task> newEvents) {
            events.clear();
            events.addAll(newEvents);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_important_event_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Task event = events.get(position);
            holder.textTitle.setText(event.getTitle());
            int color = getImportantColor(event.getImportantColor(), holder.itemView);
            holder.viewColor.setBackgroundTintList(ColorStateList.valueOf(color));
            
            // Actualizar la hora
            if (event.isHasTimeBlock() && event.getStartTime() != null) {
                holder.textTime.setText(event.getStartTime().format(timeFormatter));
            } else {
                holder.textTime.setText("Sin Hora de Inicio");
            }
            
            // Refinamiento de tipografía: Usar colorOnSurface del tema
            TypedValue typedValue = new TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            holder.textTitle.setTextColor(typedValue.data);
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

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View viewColor;
            TextView textTitle;
            TextView textTime;
            ViewHolder(View itemView) {
                super(itemView);
                viewColor = itemView.findViewById(R.id.viewColorIndicator);
                textTitle = itemView.findViewById(R.id.textEventTitle);
                textTime = itemView.findViewById(R.id.textEventTime);
            }
        }
    }

    private static class CalendarMonthAdapter extends RecyclerView.Adapter<CalendarMonthAdapter.ViewHolder> {
        private final List<CalendarDay> days = new ArrayList<>();
        private final OnCalendarDateClickListener listener;
        private final LocalDate currentWeekStart;
        private final LocalDate currentWeekEnd;

        interface OnCalendarDateClickListener {
            void onDateClick(LocalDate date);
        }

        public CalendarMonthAdapter(OnCalendarDateClickListener listener) {
            this.listener = listener;
            LocalDate today = LocalDate.now();
            currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            currentWeekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        public void setData(YearMonth month, List<Task> tasks) {
            days.clear();
            Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                    .filter(t -> t.getDeadline() != null)
                    .collect(Collectors.groupingBy(t -> t.getDeadline().toLocalDate()));

            LocalDate firstDay = month.atDay(1);
            int dayOfWeek = firstDay.getDayOfWeek().getValue();
            for (int i = 1; i < dayOfWeek; i++) {
                days.add(new CalendarDay(null, new ArrayList<>()));
            }
            for (int i = 1; i <= month.lengthOfMonth(); i++) {
                LocalDate date = month.atDay(i);
                List<Task> dayTasks = tasksByDate.getOrDefault(date, new ArrayList<>());
                days.add(new CalendarDay(date, dayTasks));
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CalendarDay day = days.get(position);
            holder.containerDots.removeAllViews();
            holder.importantHighlight.setVisibility(View.GONE);
            
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (day.date != null && !day.date.isBefore(currentWeekStart) && !day.date.isAfter(currentWeekEnd)) {
                lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, holder.itemView.getResources().getDisplayMetrics());
                int highlightColor = getThemeColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorSurfaceVariant);
                holder.itemView.setBackgroundColor(highlightColor); 
            } else {
                lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, holder.itemView.getResources().getDisplayMetrics());
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.itemView.setLayoutParams(lp);

            if (day.date == null) {
                holder.textDay.setText("");
                holder.itemView.setOnClickListener(null);
            } else {
                holder.textDay.setText(String.valueOf(day.date.getDayOfMonth()));
                List<Integer> importantColors = day.tasks.stream()
                        .filter(Task::isImportant)
                        .limit(4)
                        .map(t -> getImportantColor(t.getImportantColor(), holder.itemView))
                        .collect(Collectors.toList());

                if (!importantColors.isEmpty()) {
                    holder.importantHighlight.setVisibility(View.VISIBLE);
                    holder.importantHighlight.setColors(importantColors);
                    
                    // Fondo gris oscuro para círculos de días en Modo Noche (#2A2A2A)
                    if (isDarkMode(holder.itemView.getContext())) {
                        holder.importantHighlight.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A2A2A")));
                    }

                    double luminance = ColorUtils.calculateLuminance(importantColors.get(0));
                    holder.textDay.setTextColor(luminance > 0.5 ? Color.BLACK : Color.WHITE);
                    holder.textDay.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    if (day.date.equals(LocalDate.now())) {
                        int primaryColor = getThemeColor(holder.itemView.getContext(), androidx.appcompat.R.attr.colorPrimary);
                        holder.textDay.setTextColor(primaryColor);
                        holder.textDay.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        int onSurfaceColor = getThemeColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorOnSurface);
                        holder.textDay.setTextColor(onSurfaceColor);
                        holder.textDay.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }

                day.tasks.stream()
                        .filter(t -> t.getPriority() != null)
                        .sorted(Comparator.comparing(Task::getPriority))
                        .forEach(task -> addDot(holder.containerDots, getPriorityColor(task.getPriority(), holder.itemView.getContext())));
                
                day.tasks.stream()
                        .filter(t -> t.getPriority() == null && !t.isImportant())
                        .forEach(event -> addDot(holder.containerDots, 0xFFBDBDBD));
                
                holder.itemView.setOnClickListener(v -> listener.onDateClick(day.date));
            }
        }

        private boolean isDarkMode(Context context) {
            return (context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }

        private int getThemeColor(Context context, int attr) {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(attr, typedValue, true);
            return typedValue.data;
        }

        private void addDot(GridLayout container, int color) {
            View dot = new View(container.getContext());
            int size = (int) (6 * container.getContext().getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(2, 2, 2, 2);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.shape_dot);
            dot.setBackgroundTintList(ColorStateList.valueOf(color));
            container.addView(dot);
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

        private int getPriorityColor(Priority priority, Context context) {
            boolean isDark = isDarkMode(context);
            if (priority == null) return 0xFFBDBDBD;
            switch (priority) {
                case HIGH: return isDark ? ContextCompat.getColor(context, R.color.priority_high_dark) : 0xFFE6A4A4;
                case MEDIUM: return isDark ? ContextCompat.getColor(context, R.color.priority_medium_dark) : 0xFFF5C6A5;
                default: return isDark ? ContextCompat.getColor(context, R.color.priority_low_dark) : 0xFF9ED9B3;
            }
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textDay;
            GridLayout containerDots;
            MultiColorCircleView importantHighlight;
            ViewHolder(View itemView) {
                super(itemView);
                textDay = itemView.findViewById(R.id.textDayNumber);
                containerDots = itemView.findViewById(R.id.containerDots);
                importantHighlight = itemView.findViewById(R.id.importantHighlight);
            }
        }
    }

    private static class CalendarDay {
        final LocalDate date;
        final List<Task> tasks;
        CalendarDay(LocalDate date, List<Task> tasks) {
            this.date = date;
            this.tasks = tasks;
        }
    }
}
