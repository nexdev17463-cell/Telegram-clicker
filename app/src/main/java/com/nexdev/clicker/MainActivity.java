package com.nexdev.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;

public class MainActivity extends Activity {
    EditText username;
    TextView status, preview;
    Button fetchButton, nextButton, copyButton, keyboardButton;
    List<String> messages = new ArrayList<>();
    List<Integer> order = new ArrayList<>();
    int position = 0;
    SharedPreferences prefs;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler main = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("typing_helper", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 20);
        root.setBackgroundColor(0xFFF5F6FA);

        TextView title = new TextView(this);
        title.setText("Telegram Text Helper");
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF20243A);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Public channel se text lo, random sentence chuno aur keyboard se insert karo.");
        info.setTextSize(14);
        info.setPadding(0, 8, 0, 14);
        root.addView(info);

        username = new EditText(this);
        username.setSingleLine(true);
        username.setHint("Public channel username");
        username.setText(prefs.getString("channel", ""));
        root.addView(username);

        fetchButton = makeButton("1. Channel messages load karo");
        root.addView(fetchButton);

        status = new TextView(this);
        status.setText("Abhi channel load nahi hua.");
        status.setPadding(0, 12, 0, 12);
        root.addView(status);

        preview = new TextView(this);
        preview.setText("Selected sentence yahan dikhega.");
        preview.setTextSize(17);
        preview.setTextColor(0xFF20243A);
        preview.setPadding(14, 18, 14, 18);
        preview.setBackgroundColor(0xFFFFFFFF);
        root.addView(preview);

        nextButton = makeButton("2. Random sentence chuno");
        root.addView(nextButton);

        copyButton = makeButton("3. Sentence copy karo");
        root.addView(copyButton);

        keyboardButton = makeButton("Keyboard settings kholo");
        root.addView(keyboardButton);

        TextView note = new TextView(this);
        note.setText("Note: App Telegram par khud message send nahi karta. Bhejne se pehle text aur recipient check karo.");
        note.setTextSize(12);
        note.setPadding(0, 14, 0, 0);
        root.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);

        fetchButton.setOnClickListener(v -> loadChannel());
        nextButton.setOnClickListener(v -> chooseNext());
        copyButton.setOnClickListener(v -> copySelected());
        keyboardButton.setOnClickListener(v -> {
            try {
                startActivity(new android.content.Intent(
                    android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
                Toast.makeText(this, "Telegram Typing Helper Keyboard enable karo.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Settings open nahi hui.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            -1, -2);
        lp.setMargins(0, 8, 0, 8);
        b.setLayoutParams(lp);
        return b;
    }

    void loadChannel() {
        String name = username.getText().toString().trim().replace("@", "");
        if (name.isEmpty()) {
            username.setError("Channel username daalo");
            return;
        }
        fetchButton.setEnabled(false);
        status.setText("Channel load ho raha hai...");
        prefs.edit().putString("channel", name).apply();

        executor.execute(() -> {
            try {
                List<String> loaded = ChannelReader.fetch(name);
                main.post(() -> {
                    messages = loaded;
                    order.clear();
                    position = 0;
                    refillOrder();
                    fetchButton.setEnabled(true);
                    status.setText(messages.size() + " messages loaded.");
                    chooseNext();
                });
            } catch (Exception e) {
                main.post(() -> {
                    fetchButton.setEnabled(true);
                    status.setText("Error: " + e.getMessage());
                });
            }
        });
    }

    void refillOrder() {
        order.clear();
        for (int i = 0; i < messages.size(); i++) order.add(i);
        Collections.shuffle(order);
        position = 0;
    }

    void chooseNext() {
        if (messages.isEmpty()) {
            status.setText("Pehle channel messages load karo.");
            return;
        }
        if (position >= order.size()) {
            refillOrder();
            status.setText("Saare messages use ho gaye; naya random cycle shuru.");
        }
        String selected = messages.get(order.get(position++));
        preview.setText(selected);
        prefs.edit().putString("selected_text", selected).apply();
    }

    void copySelected() {
        String text = preview.getText().toString();
        if (text.isEmpty() || text.equals("Selected sentence yahan dikhega.")) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Selected Telegram text", text));
        prefs.edit().putString("selected_text", text).apply();
        Toast.makeText(this, "Copied! Telegram mein check karke paste kar sakte ho.", Toast.LENGTH_LONG).show();
    }
}
