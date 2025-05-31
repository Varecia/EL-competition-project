package com.tos.el;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private static final String SERVER_IP = "192.168.99.1";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private AppCompatActivity activity;
    private DataListener dataListener;
    private static final SocketClient socketClientInstance = new SocketClient();

    public interface DataListener {
        void onDataReceived(String text/*, Bitmap image*/);

        void onError(String message);
    }

    private SocketClient() {
        initConnection();
    }

    public static SocketClient getSocketClientInstance() {
        return socketClientInstance;
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    private void initConnection() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (activity != null) activity.runOnUiThread(() -> Toast.makeText(activity, "连接成功", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "连接失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    activity.finish();
                }
            }
        }).start();
    }

    public void startNavigation() {
        if (in == null) return;

        new Thread(() -> {
            try {
                out.println("START_NAVIGATION");
                this.activity.runOnUiThread(() -> Toast.makeText(activity, "定位已启动", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "启动失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void getLocationData() {
        if (in == null) return;

        new Thread(() -> {
            try {
                while (true) {
                    out.println("GET_LOCATION_DATA");
                    String head = in.readLine();
                    if (head == null || head.startsWith("NO_DATA")) {
                        Thread.sleep(5000L);
                    } else if (head.startsWith("VALID_DATA")) {
                        String locationText = in.readLine();
                        //int imageLength = Integer.parseInt(in.readLine());
                        //byte[] imageBytes = new byte[imageLength];
                        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                        //dataIn.readFully(imageBytes);
                        //Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        if (dataListener != null) {
                            dataListener.onDataReceived(locationText/*,bitmap*/);
                        }
                    }
                }
            } catch (Exception e) {
                dataListener.onError("接收数据错误：" + e.getMessage());
            }
        }).start();
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(@NonNull String content, long delay) {
        new Thread(() -> {
            try {
                out.println("REMINDER:" + content + ":" + delay);
                this.activity.runOnUiThread(() -> Toast.makeText(activity, "提醒已发送", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                this.activity.runOnUiThread(() -> Toast.makeText(activity, "发送失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    protected void sendAlertMessage() {
        new Thread(() -> {
            if (out != null) out.println("ALERT");
        }).start();
    }
}
