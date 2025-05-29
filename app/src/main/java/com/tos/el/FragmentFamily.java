package com.tos.el;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class FragmentFamily extends Fragment {
    SocketServer server=SocketServer.getSocketServerInstance();

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_family, container, false);

        MaterialButton button_navigate = view.findViewById(R.id.button_navigate);
        MaterialButton button_call = view.findViewById(R.id.button_call);
        MaterialButton button_sync = view.findViewById(R.id.button_sync);
        MaterialButton button_warn = view.findViewById(R.id.button_warn);

        button_navigate.setOnClickListener(v -> handleFamilyButtonClick(1));
        button_call.setOnClickListener(v -> handleFamilyButtonClick(2));
        button_sync.setOnClickListener(v -> handleFamilyButtonClick(3));
        button_warn.setOnClickListener(v -> handleFamilyButtonClick(4));

        return view;
    }

    private void handleFamilyButtonClick(int buttonId) {
        Intent intent = null;
        switch (buttonId) {
            case 1:
                intent = new Intent(getActivity(), NavigationActivity.class);
                break;
            case 2:
                intent = new Intent(getActivity(), ReminderInputActivity.class);
                break;
            case 3:
                intent = new Intent(getActivity(), BitmapSyncActivity.class);
                break;
            case 4:
                server.sendAlertMessage();
                break;
        }
        if (intent != null) startActivity(intent);
    }
}
