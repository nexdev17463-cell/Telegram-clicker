package com.nexdev.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    EditText channelInput;
    TextView result;
    Button fetchButton;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(Color.rgb(18, 22, 35));

        TextView title = new TextView(this);
        title.setText("Telegram Channel Reader");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        channelInput = new EditText(this);
        channelInput.setHint("@channelusername");
        channelInput.setSingleLine(true);
        channelInput.setTextColor(Color.WHITE);
        channelInput.setHintTextColor(Color.LTGRAY);
        root.addView(channelInput);

        fetchButton = new Button(this);
        fetchButton.setText("FETCH PUBLIC CHANNEL");
        root.addView(fetchButton);

        ScrollView scroll = new ScrollView(this);
        result = new TextView(this);
        result.setTextColor(Color.WHITE);
        result.setTextSize(15);
        result.setText("Channel username daal kar Fetch dabao.");
        scroll.addView(result);
        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        setContentView(root);

        fetchButton.setOnClickListener(v -> fetchChannel());
    }

    void fetchChannel() {
        String username = channelInput.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username daalo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        fetchButton.setEnabled(false);
        result.setText("Channel load ho raha hai...");

        executor.execute(() -> {
            try {
                String html = ChannelReader.fetchPublicPreview(username);
                String text = html
                        .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                        .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                        .replaceAll("(?s)<[^>]+>", " ")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replaceAll("\\s+", " ")
                        .trim();

                final String display = text.isEmpty()
                        ? "Preview mila, lekin text extract nahi hua."
                        : text.substring(0, Math.min(text.length(), 12000));

                runOnUiThread(() -> result.setText(display));
            } catch (Exception e) {
                runOnUiThread(() -> result.setText(
                        "Channel load nahi hua.\n" +
                        "Username ya internet check karo.\n\n" +
                        "Error: " + e.getMessage()));
            } finally {
                runOnUiThread(() -> fetchButton.setEnabled(true));
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
