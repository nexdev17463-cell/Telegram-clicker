package com.nexdev.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private EditText channelInput;
    private LinearLayout messageList;
    private TextView status;
    private Button fetchButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(Color.rgb(18, 22, 35));

        TextView title = new TextView(this);
        title.setText("Telegram Typing Helper");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Public channel se messages load karo, phir Use dabao.");
        info.setTextColor(Color.LTGRAY);
        root.addView(info);

        channelInput = new EditText(this);
        channelInput.setSingleLine(true);
        channelInput.setHint("@channelusername");
        channelInput.setTextColor(Color.WHITE);
        channelInput.setHintTextColor(Color.LTGRAY);
        root.addView(channelInput);

        fetchButton = new Button(this);
        fetchButton.setText("FETCH CHANNEL MESSAGES");
        root.addView(fetchButton);

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setPadding(0, 12, 0, 12);
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        messageList = new LinearLayout(this);
        messageList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(messageList);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        fetchButton.setOnClickListener(v -> fetch());
    }

    private void fetch() {
        String username = channelInput.getText().toString().trim();
        if (username.isEmpty()) {
            channelInput.setError("Channel username daalo");
            return;
        }

        fetchButton.setEnabled(false);
        status.setText("Messages load ho rahe hain...");
        messageList.removeAllViews();

        executor.execute(() -> {
            try {
                List<String> messages =
                        ChannelReader.fetchMessages(username);

                runOnUiThread(() -> {
                    if (messages.isEmpty()) {
                        status.setText(
                            "Koi text message nahi mila. Channel public hai ya nahi check karo."
                        );
                    } else {
                        status.setText("Messages: " + messages.size()
                                + " — sentence select karo");
                        for (String message : messages) addMessage(message);
                    }
                    fetchButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("Load nahi hua: " + e.getMessage());
                    fetchButton.setEnabled(true);
                });
            }
        });
    }

    private void addMessage(String message) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(14, 14, 14, 14);
        card.setBackgroundColor(Color.rgb(35, 42, 60));

        TextView body = new TextView(this);
        body.setText(message);
        body.setTextColor(Color.WHITE);
        body.setTextSize(15);
        card.addView(body);

        Button use = new Button(this);
        use.setText("USE IN TYPING KEYBOARD");
        card.addView(use);

        use.setOnClickListener(v -> {
            getSharedPreferences("typing_helper", MODE_PRIVATE)
                    .edit().putString("selected_text", message).apply();

            Toast.makeText(this,
                    "Sentence save ho gaya! Ab keyboard kholo.",
                    Toast.LENGTH_LONG).show();
        });

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 8, 0, 8);
        messageList.addView(card, lp);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
