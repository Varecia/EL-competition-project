package com.tos.el;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class FragmentBlind extends Fragment {
    SocketServer server=SocketServer.getSocketServerInstance();

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blind, container, false);

        MaterialButton button_bluetooth = view.findViewById(R.id.button_bluetooth);

        button_bluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ObjectDetectionActivity.class);
            startActivity(intent);
        });

        server.startSocketServer();

        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        if(server!=null) server.stopSocketServer();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(server!=null) server.startSocketServer();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(server!=null) server.stopSocketServer();
        server=null;
    }
}
