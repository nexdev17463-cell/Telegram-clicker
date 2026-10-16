package com.nexdev.clicker;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class ChannelReader {
    public static List<String> fetchMessages(String username)
            throws Exception {
        username = username.trim()
                .replace("https://t.me/", "")
                .replace("http://t.me/", "")
                .replace("@", "")
                .replace("/", "");

        if (!username.matches("[A-Za-z0-9_]{5,}")) {
            throw new IllegalArgumentException("Username check karo");
        }

        URL url = new URL("https://t.me/s/" + username);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(12000);
        c.setReadTimeout(12000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0");

        try {
            if (c.getResponseCode() != 200) {
                throw new IOException("Channel response: " + c.getResponseCode());
            }

            StringBuilder html = new StringBuilder();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null
                        && html.length() < 3_000_000) {
                    html.append(line).append('\n');
                }
            }

            List<String> messages = new ArrayList<>();
            Pattern p = Pattern.compile(
                "(?is)<div[^>]*class=\"[^\"]*tgme_widget_message_text[^\\\"]*\"[^>]*>(.*?)</div>"
            );
            Matcher m = p.matcher(html.toString());

            while (m.find()) {
                String s = m.group(1)
                        .replaceAll("(?i)<br\\s*/?>", "\n")
                        .replaceAll("(?is)<[^>]+>", " ")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&nbsp;", " ")
                        .replaceAll("[ \\t]+", " ")
                        .trim();

                if (!s.isEmpty()) messages.add(s);
            }

            Collections.reverse(messages);
            return messages;
        } finally {
            c.disconnect();
        }
    }
}
