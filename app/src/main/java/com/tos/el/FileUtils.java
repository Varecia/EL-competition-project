package com.tos.el;

import android.content.Context;

import java.io.File;

public class FileUtils {
    public static File getAppOutDirectory(Context context) {
        File outDir = new File(context.getFilesDir(), "out");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        return outDir;
    }

    public static String generateUniqueFilename(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (!filename.contains(".")) {
            filename += ".jpg";
        }
        return filename;
    }
}
