package com.nexdev.clicker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChannelReader {

    public static String fetchPublicPreview(String username)
            throws Exception {

        username = username.trim()
                .replace("https://t.me/", "")
                .replace("http://t.me/", "")
                .replace("@", "")
                .replace("/", "");

        if (!username.matches("[A-Za-z0-9_]{5,}")) {
            throw new IllegalArgumentException(
                    "Channel username check karo");
        }

        URL url = new URL("https://t.me/s/" + username);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty(
                "User-Agent", "Mozilla/5.0");

        try {
            int code = connection.getResponseCode();

            if (code != 200) {
                throw new Exception(
                        "Website response: " + code);
            }

            StringBuilder html = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    html.append(line).append('\n');

                    if (html.length() > 2_000_000) {
                        break;
                    }
                }
            }

            return html.toString();
        } finally {
            connection.disconnect();
        }
    }
}
