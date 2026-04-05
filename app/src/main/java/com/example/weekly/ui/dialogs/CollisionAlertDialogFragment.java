package com.example.weekly.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.weekly.R;

import java.util.List;

public class CollisionAlertDialogFragment extends DialogFragment {

    private static final String ARG_COLLISIONS = "arg_collisions";

    public static CollisionAlertDialogFragment newInstance(List<String> collisions) {
        CollisionAlertDialogFragment fragment = new CollisionAlertDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_COLLISIONS, new java.util.ArrayList<>(collisions));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TopDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_collision_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textDetails = view.findViewById(R.id.textCollisionDetails);
        List<String> collisions = getArguments().getStringArrayList(ARG_COLLISIONS);

        if (collisions != null) {
            StringBuilder sb = new StringBuilder();
            for (String info : collisions) {
                sb.append("• ").append(info).append("\n\n");
            }
            textDetails.setText(sb.toString().trim());
        }

        view.findViewById(R.id.btnOk).setOnClickListener(v -> dismiss());
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
}
