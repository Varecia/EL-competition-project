package com.tos.el;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ReminderInputActivity extends AppCompatActivity {
    private TextInputEditText etContent, etDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_input);

        etContent = findViewById(R.id.et_reminder_content);
        etDelay = findViewById(R.id.et_reminder_delay);
        MaterialButton btnSend = findViewById(R.id.btn_send_reminder);

        btnSend.setOnClickListener(v -> sendReminder());
    }

    private void sendReminder() {
        String content = "", delayStr = "";
        if (etContent.getText() != null && etDelay.getText() != null) {
            content = etContent.getText().toString().trim();
            delayStr = etDelay.getText().toString().trim();
        }

        if (content.isEmpty() || delayStr.isEmpty()) {
            Toast.makeText(this, "内容和时间不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long delay = Long.parseLong(delayStr);
            SocketServer server = SocketServer.getSocketServerInstance();
            server.sendMessage(content, delay);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
        }
    }
}
