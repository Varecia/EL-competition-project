package com.tos.el;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.WorkInfo;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class ObjectDetectionActivity extends BitmapDownloadActivity {
    private TextView resultView;
    private SocketServer server = SocketServer.getSocketServerInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_object_detection);
        controller = findViewById(R.id.button_object_detect);
        imageView = findViewById(R.id.object_detection_image);
        resultView = findViewById(R.id.object_detection_result);

        super.onCreate(savedInstanceState);

        server.initTTS(this);
        server.startSocketServer();
    }

    @Override
    public void onSuccess(WorkInfo workInfo) {
        String savedPath = workInfo.getOutputData().getString("file_path");
        Toast.makeText(this, "图片同步成功：" + savedPath, Toast.LENGTH_SHORT).show();
        bitmap = BitmapFactory.decodeFile(savedPath);
        imageView.setImageBitmap(bitmap);
        resultView.setText(R.string.detecting);

        detectObjects();
    }

    private void detectObjects() {
        if (bitmap == null) return;
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image).addOnSuccessListener(labels -> {
            StringBuilder result = new StringBuilder();
            for (ImageLabel label : labels) {
                result.append(label.getText()).append(": ").append(label.getConfidence()).append("\n");
                if (label.getConfidence() >= 0.60) {
                    if (label.getText().contains("Standing") || label.getText().contains("Walking") || label.getText().contains("Wall") || label.getText().contains("Car")) {
                        server.parseMessage("REMINDER:注意：前方有障碍物:0");
                    }
                }
            }
            resultView.setText(getString(R.string.detect_result, result.toString()));
        }).addOnFailureListener(e -> resultView.setText(R.string.detect_failure));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (server != null) server.stopSocketServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (server != null) server.startSocketServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) server.stopSocketServer();
        server = null;
    }
}
