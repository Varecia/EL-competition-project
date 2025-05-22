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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_object_detection);
        button = findViewById(R.id.button_object_detect);
        imageView = findViewById(R.id.object_detection_image);
        resultView = findViewById(R.id.object_detection_result);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSuccess(WorkInfo workInfo) {
        String savedPath = workInfo.getOutputData().getString("file_path");
        Toast.makeText(ObjectDetectionActivity.this, "图片同步成功：" + savedPath, Toast.LENGTH_SHORT).show();
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
            }
            resultView.setText(getString(R.string.detect_result, result.toString()));
        }).addOnFailureListener(e -> resultView.setText(R.string.detect_failure));
    }
}
