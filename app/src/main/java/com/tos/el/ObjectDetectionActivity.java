package com.tos.el;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class ObjectDetectionActivity extends AppCompatActivity {
    private Button button;
    private ImageView imageView;
    private TextView resultView;
    private String imageURL = "TODO"; // TODO: 这将是一个固定的地址
    private Bitmap bitmap;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean working = false;
    private static final int INTERVAL = 5000; // 目前决定每5秒进行一次识别

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        imageView = findViewById(R.id.object_detection_image);
        resultView = findViewById(R.id.object_detection_result);

        button = findViewById(R.id.button_object_detect);

        button.setOnClickListener(v -> {
            if (working) {
                stop();
            } else {
                start();
            }
        });
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
                        String savedPath = workInfo.getOutputData().getString("file_path");
                        Toast.makeText(ObjectDetectionActivity.this, "图片同步成功：" + savedPath, Toast.LENGTH_SHORT).show();
                        bitmap = BitmapFactory.decodeFile(savedPath);
                        imageView.setImageBitmap(bitmap);
                        resultView.setText(R.string.detecting);
                        detectObjects();
                        break;
                    case FAILED:
                        Toast.makeText(ObjectDetectionActivity.this, "图片未同步", Toast.LENGTH_SHORT).show();
                        break;
                    case RUNNING:
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

    private void detectObjects() {
        if (bitmap == null) return;
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image).addOnSuccessListener(labels -> {
            StringBuilder result = new StringBuilder();
            for (ImageLabel label : labels) {
                result.append(label.getText()).append(": ").append(label.getConfidence()).append("\n");
            }
            resultView.setText(getString(R.string.detect_result, result.toString()));
        }).addOnFailureListener(e -> resultView.setText(R.string.detect_failure));
    }
}
