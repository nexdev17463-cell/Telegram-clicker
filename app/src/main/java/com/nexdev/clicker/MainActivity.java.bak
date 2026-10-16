package com.nexdev.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    EditText message, delayInput;
    TextView preview;
    Button start, stop;
    volatile boolean running = false;
    Thread typingThread;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(Color.rgb(18, 22, 35));

        TextView title = new TextView(this);
        title.setText("Telegram Typing Helper");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        message = new EditText(this);
        message.setHint("Apna message likho");
        message.setTextColor(Color.WHITE);
        message.setHintTextColor(Color.LTGRAY);
        root.addView(message);

        delayInput = new EditText(this);
        delayInput.setHint("Delay milliseconds (e.g. 100)");
        delayInput.setInputType(2);
        delayInput.setText("100");
        delayInput.setTextColor(Color.WHITE);
        root.addView(delayInput);

        start = new Button(this);
        start.setText("START PREVIEW");
        root.addView(start);

        stop = new Button(this);
        stop.setText("STOP");
        root.addView(stop);

        preview = new TextView(this);
        preview.setTextColor(Color.GREEN);
        preview.setTextSize(18);
        root.addView(preview);

        setContentView(root);

        start.setOnClickListener(v -> beginPreview());
        stop.setOnClickListener(v -> {
            running = false;
            preview.setText(preview.getText() + "\nStopped");
        });
    }

    void beginPreview() {
        running = false;

        String text = message.getText().toString();
        long delay;

        try {
            delay = Long.parseLong(delayInput.getText().toString());
            if (delay < 0 || delay > 5000) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Delay 0–5000 ms rakho", Toast.LENGTH_SHORT).show();
            return;
        }

        running = true;
        preview.setText("");
        final long wait = delay;

        typingThread = new Thread(() -> {
            for (int i = 0; i < text.length() && running; i++) {
                final String current = text.substring(0, i + 1);
                runOnUiThread(() -> preview.setText(current));

                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    break;
                }
            }
            running = false;
        });
        typingThread.start();
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
