package com.nexdev.clicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.Pattern;
import android.text.Html;

public class ChannelReader {
    public static List<String> fetch(String username) throws Exception {
        username = username.trim().replace("@", "").replace("https://t.me/", "");
        if (!username.matches("[A-Za-z0-9_]{5,}")) {
            throw new Exception("Public channel ka sahi username daalo.");
        }

        URL url = new URL("https://t.me/s/" + username);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(12000);
        c.setReadTimeout(12000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder html = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(c.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) html.append(line).append('\n');
        } finally {
            c.disconnect();
        }

        List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile(
            "<div[^>]*class=\"[^\"]*tgme_widget_message_text[^\"]*\"[^>]*>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher m = p.matcher(html.toString());

        while (m.find()) {
            String text = Html.fromHtml(m.group(1), Html.FROM_HTML_MODE_LEGACY)
                    .toString().replace('\u00a0', ' ').trim();
            text = text.replaceAll("\\s+", " ");
            if (!text.isEmpty() && !result.contains(text)) result.add(text);
        }

        if (result.isEmpty()) {
            throw new Exception("Messages nahi mile. Channel public hona chahiye.");
        }
        return result;
    }
}
