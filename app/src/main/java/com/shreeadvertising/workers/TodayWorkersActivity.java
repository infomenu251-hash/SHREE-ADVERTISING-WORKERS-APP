package com.shreeadvertising.workers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodayWorkersActivity extends Activity {

    private static final String API_URL =
            "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final int NAVY = Color.rgb(16, 27, 45);
    private final int GOLD = Color.rgb(214, 173, 85);
    private final int WHITE = Color.WHITE;
    private final int GREEN = Color.rgb(32, 145, 85);

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private String language = "si";
    private String sessionToken = "";

    private TextView dateText;
    private TextView countText;
    private TextView namesText;
    private TextView messageText;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = getSharedPreferences(
                "WorkerAppSettings", MODE_PRIVATE)
                .getString("language", "si");

        sessionToken = getSharedPreferences(
                "WorkerAppSettings", MODE_PRIVATE)
                .getString("workerSessionToken", "");

        buildScreen();

        if (sessionToken.trim().isEmpty()) {
            messageText.setText(tr(
                    "කරුණාකර නැවත Login වන්න.",
                    "Please log in again."
            ));
            refreshButton.setEnabled(false);
        } else {
            loadTodayWorkers();
        }
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(25));
        scroll.addView(root);

        TextView heading = text(
                tr("අද සේවයට පැමිණි සේවකයන්",
                        "TODAY'S WORKERS"),
                21, NAVY, true);

        heading.setGravity(Gravity.CENTER);
        heading.setPadding(dp(12), dp(22), dp(12), dp(22));
        heading.setBackground(round(GOLD, 15));
        root.addView(heading);

        dateText = text("", 16, WHITE, true);
        dateText.setGravity(Gravity.CENTER);
        dateText.setPadding(0, dp(20), 0, dp(15));
        root.addView(dateText);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(17), dp(20), dp(17), dp(20));
        card.setBackground(round(Color.rgb(35, 49, 68), 14));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        cardParams.setMargins(0, dp(8), 0, dp(12));
        root.addView(card, cardParams);

        countText = text("", 17, GOLD, true);
        countText.setGravity(Gravity.CENTER);
        countText.setPadding(0, 0, 0, dp(18));
        card.addView(countText);

        namesText = text(
                tr("දත්ත ලබාගනිමින්...", "Loading workers..."),
                17, WHITE, false);

        namesText.setGravity(Gravity.CENTER);
        namesText.setLineSpacing(dp(8), 1.0f);
        card.addView(namesText);

        refreshButton = button(
                tr("නැවත යාවත්කාලීන කරන්න", "REFRESH LIST"),
                GREEN, WHITE);

        LinearLayout.LayoutParams refreshParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(55));

        refreshParams.setMargins(0, dp(8), 0, dp(5));
        root.addView(refreshButton, refreshParams);

        refreshButton.setOnClickListener(v -> loadTodayWorkers());

        messageText = text("", 13, WHITE, false);
        messageText.setGravity(Gravity.CENTER);
        messageText.setPadding(dp(5), dp(10), dp(5), dp(10));
        root.addView(messageText);

        Button back = button(
                tr("ආපසු", "BACK"),
                Color.DKGRAY, WHITE);

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(55));

        backParams.setMargins(0, dp(8), 0, dp(5));
        root.addView(back, backParams);

        back.setOnClickListener(v -> finish());

        setContentView(scroll);

        dateText.setText(
                new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.getDefault()
                ).format(new Date())
        );
    }

    private void loadTodayWorkers() {
        if (sessionToken.trim().isEmpty()) return;

        refreshButton.setEnabled(false);

        namesText.setText(
                tr("දත්ත ලබාගනිමින්...", "Loading workers..."));

        messageText.setText("");

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "getTodayWorkingStaff");
                request.put("sessionToken", sessionToken);

                JSONObject result = postJson(request);

                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);

                    if (!result.optBoolean("success", false)) {
                        namesText.setText(
                                tr("ලැයිස්තුව ලබාගත නොහැක.",
                                        "Unable to load workers."));

                        messageText.setText(result.optString(
                                "message",
                                tr("නැවත උත්සාහ කරන්න.",
                                        "Please try again.")
                        ));
                        return;
                    }

                    JSONArray names = result.optJSONArray("names");

                    if (names == null || names.length() == 0) {
                        countText.setText(
                                tr("සේවකයන් ගණන: 0",
                                        "WORKERS: 0"));

                        namesText.setText(
                                tr("අද තවම කිසිවෙකු පැමිණීම සටහන් කර නැත.",
                                        "No workers have checked in today."));
                        return;
                    }

                    countText.setText(
                            tr("සේවකයන් ගණන: " + names.length(),
                                    "WORKERS: " + names.length())
                    );

                    StringBuilder output = new StringBuilder();

                    for (int i = 0; i < names.length(); i++) {
                        String name = names.optString(i, "").trim();

                        if (!name.isEmpty()) {
                            output.append("•  ")
                                    .append(name)
                                    .append("\n\n");
                        }
                    }

                    namesText.setText(output.toString().trim());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);

                    namesText.setText(
                            tr("දත්ත ලබාගැනීමේ දෝෂයකි.",
                                    "Error loading workers."));

                    messageText.setText(
                            tr("Internet සම්බන්ධතාව පරීක්ෂා කරන්න.",
                                    "Please check your internet connection."));
                });
            }
        });
    }

    private JSONObject postJson(JSONObject request) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8");

            byte[] body = request.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }

            int responseCode = connection.getResponseCode();

            InputStream stream;

            if (responseCode >= 200 && responseCode < 400) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }

            if (stream == null) {
                throw new Exception("Server response unavailable.");
            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            stream, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return new JSONObject(response.toString());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Button button(String value, int background, int foreground) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(foreground);
        b.setTextSize(14);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(round(background, 12));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(null, Typeface.BOLD);
        }

        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private String tr(String sinhala, String english) {
        return language.equals("si") ? sinhala : english;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
