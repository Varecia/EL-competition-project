package com.tos.el;

import android.os.Bundle;

public class BitmapSyncActivity extends BitmapDownloadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_bitmap_sync);
        controller = findViewById(R.id.button_bitmap_sync);
        imageView = findViewById(R.id.bitmap_sync_image);

        super.onCreate(savedInstanceState);
    }
}
