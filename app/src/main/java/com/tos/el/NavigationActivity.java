package com.tos.el;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Locale;

public class NavigationActivity extends ScheduledUpdateActivity {
    private FusedLocationProviderClient fusedLocationClient;
    protected TextView locationText;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_navigation);
        locationText = findViewById(R.id.location_text);
        controller = findViewById(R.id.button_navigation);
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        setInterval(10000L);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "位置权限获取成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void update() {
        locationText.setText(R.string.navigating);
        getLastKnownLocation();
        requestNewLocation();
    }

    private void getLastKnownLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, this::updateUI).addOnFailureListener(e-> Toast.makeText(this,"定位失败",Toast.LENGTH_SHORT).show());
        }
    }

    private void requestNewLocation() {
        if (checkLocationPermission()) {
            LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2*getInterval()).setMinUpdateIntervalMillis(getInterval()).build();
            LocationCallback callback=new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    updateUI(locationResult.getLastLocation());
                    fusedLocationClient.removeLocationUpdates(this);
                }
            };
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
            Toast.makeText(this,"正在请求最新位置",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(@Nullable Location location) {
        if (location == null) return;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        locationText.setText(String.format(new Locale.Builder().setLanguage("zh").setRegion("CN").build(), "纬度：%.6f\n经度：%.6f", latitude, longitude));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient = null;
    }
}
