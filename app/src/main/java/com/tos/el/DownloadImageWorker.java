package com.tos.el;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageWorker extends Worker {

    public DownloadImageWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String imageUrl = getInputData().getString("url");

        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.failure();
        }

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            File outDir = FileUtils.getAppOutDirectory(getApplicationContext());
            String filename = FileUtils.generateUniqueFilename(imageUrl);
            File outputFile = new File(outDir, filename);

            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            Data outputData = new Data.Builder().putString("file_path", outputFile.getAbsolutePath()).build();
            return Result.success(outputData);
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
