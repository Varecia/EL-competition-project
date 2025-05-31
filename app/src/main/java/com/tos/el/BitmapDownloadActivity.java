package com.tos.el;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public abstract class BitmapDownloadActivity extends ScheduledUpdateActivity {
    protected ImageView imageView;
    protected String imageURL = "http://192.168.99.219//capture";
    protected Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInterval(2000L);
    }

    public void onSuccess(WorkInfo workInfo) {
        String savedPath = workInfo.getOutputData().getString("file_path");
        Toast.makeText(BitmapDownloadActivity.this, "图片同步成功：" + savedPath, Toast.LENGTH_SHORT).show();
        bitmap = BitmapFactory.decodeFile(savedPath);
        if (imageView != null) imageView.setImageBitmap(bitmap);
    }

    public void onFailure(WorkInfo workInfo) {
        Toast.makeText(BitmapDownloadActivity.this, "图片未同步", Toast.LENGTH_SHORT).show();
    }

    public void running(WorkInfo workInfo) {
        Toast.makeText(BitmapDownloadActivity.this, "下载中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public final void update() {
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
}
