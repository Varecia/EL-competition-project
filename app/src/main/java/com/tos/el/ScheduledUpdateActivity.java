package com.tos.el;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public abstract class ScheduledUpdateActivity extends AppCompatActivity {
    protected Button controller;
    private Runnable scheduledUpdateRunner;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isWorking = false;
    private long interval = 5000L;

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (controller == null) {
            start();
        } else {
            controller.setOnClickListener(v -> {
                if (isWorking) stop();
                else start();
            });
        }
        super.onCreate(savedInstanceState);
    }

    public abstract void update();

    private void start() {
        if (isWorking) return;
        isWorking = true;
        if (controller != null) controller.setText(R.string.stop);

        scheduledUpdateRunner = new Runnable() {
            @Override
            public void run() {
                update();
                if (isWorking) {
                    handler.postDelayed(this, interval);
                }
            }
        };

        handler.postDelayed(scheduledUpdateRunner, 0);
    }

    private void stop() {
        if (!isWorking) return;
        isWorking = false;
        if (controller != null) controller.setText(R.string.start);

        handler.removeCallbacks(scheduledUpdateRunner);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
}
