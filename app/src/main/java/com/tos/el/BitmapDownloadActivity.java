package com.tos.el;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public abstract class BitmapDownloadActivity extends AppCompatActivity {
    protected Button button;
    protected ImageView imageView;
    protected String imageURL = "http://192.168.43.66/capture";
    protected Bitmap bitmap;
    protected boolean working = false;
    protected Runnable runnable;
    private final Handler handler = new Handler();
    private static final int INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(button==null) Toast.makeText(BitmapDownloadActivity.this,"未设置按钮",Toast.LENGTH_SHORT).show();
        else{
            button.setOnClickListener(v -> {
                if (working) stop();
                else start();
            });
        }
        super.onCreate(savedInstanceState);
    }

    public void onSuccess(WorkInfo workInfo){
        String savedPath = workInfo.getOutputData().getString("file_path");
        Toast.makeText(BitmapDownloadActivity.this, "图片同步成功：" + savedPath, Toast.LENGTH_SHORT).show();
        bitmap = BitmapFactory.decodeFile(savedPath);
        if (imageView != null) imageView.setImageBitmap(bitmap);
    }

    public void onFailure(WorkInfo workInfo){
        Toast.makeText(BitmapDownloadActivity.this, "图片未同步", Toast.LENGTH_SHORT).show();
    }

    public void running(WorkInfo workInfo){
        Toast.makeText(BitmapDownloadActivity.this, "下载中", Toast.LENGTH_SHORT).show();
    }

    public void downloadBitmap() {
        if (imageURL == null) return;
        Data url = new Data.Builder().putString("url", imageURL).build();
        OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(DownloadImageWorker.class).setInputData(url).build();
        WorkManager.getInstance(this).enqueue(downloadWorkRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(this, workInfo -> {
            if (workInfo != null) {
                switch (workInfo.getState()) {
                    case SUCCEEDED:
                        onSuccess(workInfo);
                        break;
                    case FAILED:
                        onFailure(workInfo);
                        break;
                    case RUNNING:
                        running(workInfo);
                        break;
                }
            }
        });
    }

    private void start() {
        if (working) return;
        working = true;
        button.setText(R.string.stop);

        runnable = new Runnable() {
            @Override
            public void run() {
                downloadBitmap();
                if (working) {
                    handler.postDelayed(this, INTERVAL);
                }
            }
        };

        handler.postDelayed(runnable, INTERVAL);
    }

    private void stop() {
        if (!working) return;
        working = false;
        button.setText(R.string.start);

        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
}
