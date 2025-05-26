package com.tos.el;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

public class FileUtils {
    @NonNull
    public static File getAppOutDirectory(@NonNull Context context) {
        File outDir = new File(context.getFilesDir(), "out");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        return outDir;
    }

    @NonNull
    public static String generateUniqueFilename(@NonNull String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (!filename.contains(".")) {
            filename += ".jpg";
        }
        return filename;
    }
}
