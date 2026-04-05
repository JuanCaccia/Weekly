package com.example.weekly.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.weekly.R;
import com.example.weekly.domain.SpleetHeader;
import com.example.weekly.ui.viewmodels.SpleetViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SelectSpleetDialogFragment extends DialogFragment {

    public static SelectSpleetDialogFragment newInstance() {
        return new SelectSpleetDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TopDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_select_spleet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SpleetViewModel viewModel = new ViewModelProvider(requireActivity()).get(SpleetViewModel.class);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupSpleets);
        TextInputEditText editNewName = view.findViewById(R.id.editNewSpleetName);
        Button btnCreate = view.findViewById(R.id.btnCreateSpleet);

        viewModel.allHeaders.observe(getViewLifecycleOwner(), headers -> {
            setupChips(chipGroup, headers, viewModel);
        });

        btnCreate.setOnClickListener(v -> {
            String name = editNewName.getText().toString().trim();
            if (!name.isEmpty()) {
                viewModel.createNewSpleet(name);
                dismiss();
            }
        });

        getParentFragmentManager().setFragmentResultListener("spleet_selected", getViewLifecycleOwner(), (requestKey, result) -> {
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.TOP);
                WindowManager.LayoutParams params = window.getAttributes();
                params.y = 0;
                window.setAttributes(params);
            }
        }
    }

    private void setupChips(ChipGroup group, List<SpleetHeader> headers, SpleetViewModel viewModel) {
        group.removeAllViews();
        if (headers == null) return;

        Long currentId = viewModel.getCurrentHeaderId();

        for (SpleetHeader header : headers) {
            Chip chip = new Chip(getContext());
            chip.setText(header.name);
            chip.setCheckable(true);
            if (header.id != null && header.id.equals(currentId)) {
                chip.setChecked(true);
            }
            chip.setOnClickListener(v -> {
                SpleetOptionsDialogFragment.newInstance(header.id, header.name)
                        .show(getParentFragmentManager(), "spleet_options");
            });
            group.addView(chip);
        }
    }
}
