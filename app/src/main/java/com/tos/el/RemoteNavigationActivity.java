package com.tos.el;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteNavigationActivity extends ScheduledUpdateActivity {
    private TextView locationText;
    private ImageView map;
    private SocketClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_navigation);
        locationText = findViewById(R.id.location_text);
        map = findViewById(R.id.image_location);
        controller = findViewById(R.id.button_navigation);
        super.onCreate(savedInstanceState);
        setInterval(10000L);
        client = new SocketClient(this, new SocketClient.DataListener() {
            @Override
            public void onDataReceived(String text, Bitmap image) {
                runOnUiThread(() -> {
                    locationText.setText(text);
                    map.setImageBitmap(image);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(RemoteNavigationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void update() {
        locationText.setText(R.string.navigating);
        client.getLocationData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) client.close();
        client = null;
    }
}
