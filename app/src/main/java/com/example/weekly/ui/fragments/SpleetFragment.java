package com.example.weekly.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.SpleetHeader;
import com.example.weekly.domain.SpleetTask;
import com.example.weekly.ui.adapters.SpleetTaskAdapter;
import com.example.weekly.ui.dialogs.CollisionAlertDialogFragment;
import com.example.weekly.ui.dialogs.EditSpleetTaskDialog;
import com.example.weekly.ui.dialogs.SelectSpleetDialogFragment;
import com.example.weekly.ui.viewmodels.SpleetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpleetFragment extends Fragment {
    private SpleetViewModel viewModel;
    private SpleetTaskAdapter adapter;
    private EditText editSpleetName;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spleet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SpleetViewModel.class);

        editSpleetName = view.findViewById(R.id.editSpleetName);
        RecyclerView rv = view.findViewById(R.id.rvSpleetTasks);
        MaterialButton btnApply = view.findViewById(R.id.btnApplySpleet);
        ImageView iconEdit = view.findViewById(R.id.iconEditSpleet);
        View mainContainer = view.findViewById(R.id.mainContainer);
        layoutEmpty = view.findViewById(R.id.layoutEmptySpleet);

        adapter = new SpleetTaskAdapter(new SpleetTaskAdapter.OnSpleetInteractionListener() {
            @Override
            public void onEdit(SpleetTask task) {
                EditSpleetTaskDialog.newInstanceForEdit(task.id)
                        .show(getParentFragmentManager(), "edit_spleet_task");
            }

            @Override
            public void onDelete(SpleetTask task) {
                viewModel.deleteSpleetTask(task);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Quitar foco al tocar el fondo o la lista
        View.OnClickListener clearFocusListener = v -> {
            if (editSpleetName.hasFocus()) {
                editSpleetName.clearFocus();
                hideKeyboard(v);
            }
        };
        mainContainer.setOnClickListener(clearFocusListener);
        rv.setOnTouchListener((v, event) -> {
            clearFocusListener.onClick(v);
            return false;
        });

        viewModel.spleetTasks.observe(getViewLifecycleOwner(), tasks -> {
            adapter.setSpleetTasks(tasks);
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // Observamos el ID actual y la lista de headers para actualizar el nombre
        viewModel.currentHeaderId.observe(getViewLifecycleOwner(), id -> updateTitle(id, viewModel.allHeaders.getValue()));
        viewModel.allHeaders.observe(getViewLifecycleOwner(), headers -> updateTitle(viewModel.getCurrentHeaderId(), headers));

        iconEdit.setOnClickListener(v -> {
            SelectSpleetDialogFragment.newInstance().show(getParentFragmentManager(), "select_spleet");
        });

        iconEdit.setOnLongClickListener(v -> {
            showSpleetOptions();
            return true;
        });

        view.findViewById(R.id.fabAddSpleetTask).setOnClickListener(v -> {
            EditSpleetTaskDialog.newInstance()
                    .show(getParentFragmentManager(), "add_spleet_task");
        });

        btnApply.setOnClickListener(v -> {
            showApplyOptions();
        });

        viewModel.collisionNotifications.observe(getViewLifecycleOwner(), collisions -> {
            if (collisions != null && !collisions.isEmpty()) {
                CollisionAlertDialogFragment.newInstance(collisions)
                        .show(getParentFragmentManager(), "collision_alert");
                viewModel.clearProjectionState();
            } else if (collisions != null) {
                Toast.makeText(getContext(), "Spleet aplicado correctamente", Toast.LENGTH_SHORT).show();
                viewModel.clearProjectionState();
            }
        });

        editSpleetName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setSpleetName(editSpleetName.getText().toString());
            }
        });

        editSpleetName.setOnEditorActionListener((v, actionId, event) -> {
            editSpleetName.clearFocus();
            hideKeyboard(v);
            return true;
        });
    }

    private void showApplyOptions() {
        ApplySpleetBottomSheet bottomSheet = new ApplySpleetBottomSheet(new ApplySpleetBottomSheet.OnApplyOptionListener() {
            @Override
            public void onApplyThisWeek() {
                viewModel.applyToDate(LocalDate.now());
            }

            @Override
            public void onApplyNextWeek() {
                viewModel.applyToDate(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
            }

            @Override
            public void onSelectCustomDate() {
                showDatePicker();
            }
        });
        bottomSheet.show(getParentFragmentManager(), "apply_spleet_options");
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar semana de inicio")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            // Ajustar al lunes de esa semana para mostrar feedback al usuario
            LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = monday.plusDays(6);
            
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            String weekRange = monday.format(fmt) + " al " + sunday.format(fmt);
            
            Toast.makeText(getContext(), "Aplicando a la semana del " + weekRange, Toast.LENGTH_LONG).show();

            viewModel.applyToDate(selectedDate);
        });

        datePicker.show(getParentFragmentManager(), "date_picker");
    }

    private void showSpleetOptions() {
        SpleetOptionsBottomSheet bottomSheet = new SpleetOptionsBottomSheet(editSpleetName.getText().toString(), () -> {
            viewModel.deleteCurrentSpleet();
        });
        bottomSheet.show(getParentFragmentManager(), "spleet_options");
    }

    private void updateTitle(Long currentId, List<SpleetHeader> headers) {
        if (currentId != null && headers != null) {
            for (SpleetHeader h : headers) {
                if (h.id.equals(currentId)) {
                    String currentName = editSpleetName.getText().toString();
                    if (!currentName.equals(h.name)) {
                        editSpleetName.setText(h.name);
                    }
                    break;
                }
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static class ApplySpleetBottomSheet extends BottomSheetDialogFragment {
        public interface OnApplyOptionListener {
            void onApplyThisWeek();
            void onApplyNextWeek();
            void onSelectCustomDate();
        }

        private final OnApplyOptionListener listener;

        public ApplySpleetBottomSheet(OnApplyOptionListener listener) {
            this.listener = listener;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_apply_spleet_options, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.findViewById(R.id.btnApplyThisWeek).setOnClickListener(v -> {
                listener.onApplyThisWeek();
                dismiss();
            });
            view.findViewById(R.id.btnApplyNextWeek).setOnClickListener(v -> {
                listener.onApplyNextWeek();
                dismiss();
            });
            view.findViewById(R.id.btnSelectStartDate).setOnClickListener(v -> {
                listener.onSelectCustomDate();
                dismiss();
            });
        }
    }

    public static class SpleetOptionsBottomSheet extends BottomSheetDialogFragment {
        private final String name;
        private final Runnable onDelete;

        public SpleetOptionsBottomSheet(String name, Runnable onDelete) {
            this.name = name;
            this.onDelete = onDelete;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_spleet_options, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((TextView)view.findViewById(R.id.textSpleetName)).setText(name);
            view.findViewById(R.id.btnDeleteSpleet).setOnClickListener(v -> {
                onDelete.run();
                dismiss();
            });
        }
    }
}
