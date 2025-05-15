package com.tos.el;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class FragmentBlind extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blind, container, false);

        MaterialButton button_bluetooth = view.findViewById(R.id.button_bluetooth);

        button_bluetooth.setOnClickListener(v -> handleBlindButtonClick(1));


        return view;
    }

    private void handleBlindButtonClick(int buttonId) {
        //TODO: onClick
    }
}
