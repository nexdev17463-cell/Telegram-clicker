package com.nexdev.clicker;

import android.inputmethodservice.InputMethodService;
import android.graphics.Color;
import android.view.View;
import android.view.Gravity;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TypingKeyboardService extends InputMethodService {

    private EditText textInput;
    private Button typeButton;
    private volatile boolean running = false;
    private Thread typingThread;

    @Override
    public View onCreateInputView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12, 12, 12, 12);
        root.setBackgroundColor(Color.rgb(24, 29, 44));

        TextView title = new TextView(this);
        title.setText("Typing Helper • Review before sending");
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        textInput = new EditText(this);
        textInput.setHint("Paste or type your text");
        textInput.setTextColor(Color.WHITE);
        textInput.setHintTextColor(Color.LTGRAY);
        root.addView(textInput);

        EditText delayInput = new EditText(this);
        delayInput.setHint("Delay per character (ms)");
        delayInput.setInputType(2);
        delayInput.setText("100");
        delayInput.setTextColor(Color.WHITE);
        root.addView(delayInput);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        typeButton = new Button(this);
        typeButton.setText("TYPE");
        buttons.addView(typeButton, new LinearLayout.LayoutParams(
                0, -2, 1));

        Button stopButton = new Button(this);
        stopButton.setText("STOP");
        buttons.addView(stopButton, new LinearLayout.LayoutParams(
                0, -2, 1));

        root.addView(buttons);

        typeButton.setOnClickListener(v -> {
            if (running) return;

            String text = textInput.getText().toString();
            long delay;

            try {
                delay = Long.parseLong(
                        delayInput.getText().toString());
                if (delay < 1 || delay > 5000) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Delay 1–5000 ms rakho",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (text.isEmpty()) {
                Toast.makeText(this, "Pehle text daalo",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            InputConnection connection = getCurrentInputConnection();
            if (connection == null) {
                Toast.makeText(this, "Pehle message field select karo",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            running = true;
            typeButton.setEnabled(false);

            typingThread = new Thread(() -> {
                try {
                    for (int i = 0; i < text.length() && running; i++) {
                        final String character = text.substring(i, i + 1);

                        runOnUiThread(() -> {
                            if (running) {
                                InputConnection current =
                                        getCurrentInputConnection();
                                if (current != null) {
                                    current.commitText(character, 1);
                                }
                            }
                        });

                        Thread.sleep(delay);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    running = false;
                    runOnUiThread(() -> typeButton.setEnabled(true));
                }
            });
            typingThread.start();
        });

        stopButton.setOnClickListener(v -> {
            running = false;
            if (typingThread != null) typingThread.interrupt();
            typeButton.setEnabled(true);
        });

        return root;
    }

    @Override
    public void onDestroy() {
        running = false;
        if (typingThread != null) typingThread.interrupt();
        super.onDestroy();
    }
}
