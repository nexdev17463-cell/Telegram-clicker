package com.nexdev.clicker;

import android.inputmethodservice.InputMethodService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TypingKeyboardService extends InputMethodService {
    @Override
    public View onCreateInputView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(8, 8, 8, 8);
        layout.setBackgroundColor(Color.rgb(235, 238, 245));

        TextView title = new TextView(this);
        title.setText("Telegram Text Helper");
        title.setTextSize(16);
        title.setTextColor(Color.BLACK);
        title.setPadding(8, 8, 8, 8);
        layout.addView(title);

        Button insert = new Button(this);
        insert.setText("Insert selected sentence");
        layout.addView(insert);

        Button space = new Button(this);
        space.setText("Space");
        layout.addView(space);

        Button hide = new Button(this);
        hide.setText("Hide keyboard");
        layout.addView(hide);

        insert.setOnClickListener(v -> {
            SharedPreferences p = getSharedPreferences("typing_helper", MODE_PRIVATE);
            String text = p.getString("selected_text", "");
            if (text.isEmpty()) {
                Toast.makeText(this, "Pehle app mein sentence select karo.", Toast.LENGTH_SHORT).show();
                return;
            }
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(text, 1);
        });

        space.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" ", 1);
        });

        hide.setOnClickListener(v -> requestHideSelf(0));
        return layout;
    }
}
