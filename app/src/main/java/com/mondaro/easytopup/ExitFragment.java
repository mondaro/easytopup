package com.mondaro.easytopup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mondaro.easytopup.ui.BaseFragment;

public class ExitFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exit, container, false);

        root.findViewById(R.id.buttonExit).setOnClickListener(v -> requireActivity().finish());
        root.findViewById(R.id.buttonCancel).setOnClickListener(v ->
                ((MainActivity) requireActivity()).displayView(
                        prefs().isConfigured() ? MainActivity.VIEW_TOPUP : MainActivity.VIEW_SETTING));

        return root;
    }
}
