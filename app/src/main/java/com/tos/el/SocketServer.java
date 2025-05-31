package com.tos.el;

import android.content.Intent;
//import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class SocketServer {
    private static final SocketServer socketServerInstance = new SocketServer();
    private AppCompatActivity activity;
    protected TextToSpeech textToSpeech;
    protected ServerSocket serverSocket;
    protected boolean isServerRunning = false;
    //protected Bitmap currentMap;
    protected String currentLocation;
    private Socket client; //由于设备限制，我们目前只接受单一设备连接

    private SocketServer() {
    }

    public void initTTS(AppCompatActivity activity) {
        this.activity = activity;
        if (this.activity != null) {
            textToSpeech = new TextToSpeech(this.activity, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this.activity, "语言不支持", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(this.activity, "初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this.activity, "初始化失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static SocketServer getSocketServerInstance() {
        return socketServerInstance;
    }

    protected void startSocketServer() {
        if (isServerRunning) return;

        isServerRunning = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8080);
                while (isServerRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClientConnection(clientSocket);
                }
            } catch (IOException e) {
                if (isServerRunning) {
                    activity.runOnUiThread(() -> Toast.makeText(this.activity, "服务器错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    protected void stopSocketServer() {
        isServerRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverSocket = null;
    }

    private void handleClientConnection(Socket clientSocket) {
        new Thread(() -> {
            try {
                client = clientSocket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    parseMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void parseMessage(String message) {
        if (isServerRunning) {
            if (message.startsWith("ALERT")) {
                playAlertSound();
            } else if (message.startsWith("START_NAVIGATION")) {
                startNavigation();
            } else if (message.startsWith("GET_LOCATION_DATA")) {
                sendLocationData();
            } else if (message.startsWith("REMINDER")) {
                String[] parts = message.split(":");
                if (parts.length >= 3) {
                    String content = parts[1];
                    long delaySeconds = Long.parseLong(parts[2]);
                    scheduleReminder(content, delaySeconds);
                }
            }
        }
    }

    protected void playAlertSound() {
        if (isServerRunning) {
            activity.runOnUiThread(() -> {
                try {
                    MediaPlayer mediaPlayer = MediaPlayer.create(this.activity, R.raw.alert);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                } catch (Exception e) {
                    Toast.makeText(this.activity, "播放警报失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected void scheduleReminder(@NonNull String content, long delaySeconds) {
        if (isServerRunning) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> showReminder(content), 30);
        }
    }

    protected void showReminder(@NonNull String content) {
        activity.runOnUiThread(() -> {
            Toast.makeText(this.activity, content, Toast.LENGTH_LONG).show();
            if (textToSpeech != null) {
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    public void startNavigation() {
        if (isServerRunning) {
            Intent intent = new Intent(activity, LocalNavigationActivity.class);
            activity.startActivity(intent);
        }
    }

    public void setCurrentLocation(String location) {
        currentLocation = location;
    }

    private void sendLocationData() {
        OutputStream out = null;
        PrintWriter writer = null;
        try {
            out = client.getOutputStream();
            writer = new PrintWriter(out, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (out == null) return;

        if (currentLocation == null /*|| currentMap == null*/) {
            writer.println("NO_DATA");
        }

        //ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        //currentMap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream);
        //byte[] mapBytes = byteStream.toByteArray();

        writer.println("VALID_DATA");
        writer.println(currentLocation);
        //writer.println(mapBytes.length);

        try {
            //out.write(mapBytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
