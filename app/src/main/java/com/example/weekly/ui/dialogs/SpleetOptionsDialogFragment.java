package com.example.weekly.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.weekly.R;
import com.example.weekly.ui.viewmodels.SpleetViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SpleetOptionsDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_SPLEET_ID = "spleet_id";
    private static final String ARG_SPLEET_NAME = "spleet_name";

    public static SpleetOptionsDialogFragment newInstance(Long id, String name) {
        SpleetOptionsDialogFragment fragment = new SpleetOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SPLEET_ID, id);
        args.putString(ARG_SPLEET_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_spleet_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SpleetViewModel viewModel = new ViewModelProvider(requireActivity()).get(SpleetViewModel.class);

        Long spleetId = getArguments().getLong(ARG_SPLEET_ID);
        String spleetName = getArguments().getString(ARG_SPLEET_NAME);

        TextView textTitle = view.findViewById(R.id.textSpleetName);
        textTitle.setText(spleetName);

        view.findViewById(R.id.btnGoToSpleet).setOnClickListener(v -> {
            viewModel.selectSpleet(spleetId);
            dismiss();
            getParentFragmentManager().setFragmentResult("spleet_selected", new Bundle());
        });

        view.findViewById(R.id.btnDeleteSpleet).setOnClickListener(v -> {
            viewModel.deleteSpleetById(spleetId);
            dismiss();
        });
    }
}
