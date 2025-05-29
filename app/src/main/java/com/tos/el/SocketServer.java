package com.tos.el;

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
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class SocketServer {
    private static final SocketServer socketServerInstance = new SocketServer();
    AppCompatActivity activity;
    protected TextToSpeech textToSpeech;
    protected ServerSocket serverSocket;
    protected boolean isServerRunning = false;

    private SocketServer() {
        if (this.activity != null) {
            textToSpeech = new TextToSpeech(this.activity, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this.activity, "语言不支持", Toast.LENGTH_SHORT).show();
                    }
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
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

    protected void sendAlertMessage() {
        if (isServerRunning) {
            parseMessage("ALERT");
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
            new Handler(Looper.getMainLooper()).postDelayed(() -> showReminder(content), delaySeconds * 60000);
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

    public void sendMessage(@NonNull String content, long delay) {
        if (isServerRunning) {
            new Thread(() -> {
                try {
                    Socket socket = new Socket("192.168.43.1", 8080);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println("REMINDER:" + content + ":" + delay);
                    writer.close();
                    socket.close();

                    this.activity.runOnUiThread(() -> Toast.makeText(activity, "提醒已发送", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    this.activity.runOnUiThread(() -> Toast.makeText(activity, "发送失败", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }
}
