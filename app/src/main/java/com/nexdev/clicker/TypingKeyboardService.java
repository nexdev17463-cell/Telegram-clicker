package com.nexdev.clicker;

import android.inputmethodservice.InputMethodService;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.*;

public class TypingKeyboardService extends InputMethodService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private EditText textInput, delayInput;
    private Button typeButton;
    private boolean running = false;
    private String textToType = "";
    private int position = 0;
    private long delay = 100;

    private final Runnable typingTask = new Runnable() {
        @Override public void run() {
            if (!running) return;

            if (position >= textToType.length()) {
                stopTyping();
                Toast.makeText(TypingKeyboardService.this,
                        "Typing complete", Toast.LENGTH_SHORT).show();
                return;
            }

            InputConnection ic = getCurrentInputConnection();
            if (ic == null) {
                stopTyping();
                return;
            }

            ic.commitText(textToType.substring(position, position + 1), 1);
            position++;
            handler.postDelayed(this, delay);
        }
    };

    @Override
    public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12, 12, 12, 12);
        root.setBackgroundColor(Color.rgb(24, 29, 44));

        TextView title = new TextView(this);
        title.setText("Typing Helper");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        textInput = new EditText(this);
        textInput.setHint("Selected channel message");
        textInput.setTextColor(Color.WHITE);
        textInput.setHintTextColor(Color.LTGRAY);
        textInput.setMinLines(2);
        textInput.setMaxLines(4);

        String saved = getSharedPreferences("typing_helper", MODE_PRIVATE)
                .getString("selected_text", "");
        textInput.setText(saved);
        root.addView(textInput);

        delayInput = new EditText(this);
        delayInput.setHint("Delay per character (ms)");
        delayInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        delayInput.setText("100");
        delayInput.setTextColor(Color.WHITE);
        root.addView(delayInput);

        LinearLayout row = new LinearLayout(this);
        typeButton = new Button(this);
        typeButton.setText("TYPE");
        row.addView(typeButton, new LinearLayout.LayoutParams(0, -2, 1));

        Button stop = new Button(this);
        stop.setText("STOP");
        row.addView(stop, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row);

        typeButton.setOnClickListener(v -> startTyping());
        stop.setOnClickListener(v -> stopTyping());
        return root;
    }

    private void startTyping() {
        if (running) return;

        textToType = textInput.getText().toString();
        if (textToType.isEmpty()) {
            Toast.makeText(this, "Pehle channel se message select karo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            delay = Long.parseLong(delayInput.getText().toString().trim());
            if (delay < 1 || delay > 5000) throw new NumberFormatException();
        } catch (Exception e) {
            Toast.makeText(this, "Delay 1–5000 ms rakho",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (getCurrentInputConnection() == null) {
            Toast.makeText(this, "Telegram ka message box select karo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        position = 0;
        running = true;
        typeButton.setEnabled(false);
        handler.post(typingTask);
    }

    private void stopTyping() {
        running = false;
        handler.removeCallbacks(typingTask);
        if (typeButton != null) typeButton.setEnabled(true);
    }

    @Override public void onFinishInput() {
        stopTyping();
        super.onFinishInput();
    }

    @Override public void onDestroy() {
        stopTyping();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
