package com.shreeadvertising.workers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

public class AttendanceActivity extends Activity {

    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final int NAVY = Color.rgb(16, 27, 45);
    private final int GOLD = Color.rgb(214, 173, 85);
    private final int WHITE = Color.WHITE;
    private final int GREEN = Color.rgb(32, 145, 85);
    private final int RED = Color.rgb(200, 45, 45);

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    private String language = "si";
    private String sessionToken = "";

    private TextView todayStatus;
    private TextView checkInText;
    private TextView checkOutText;
    private TextView totalHoursText;
    private TextView historyText;
    private TextView messageText;

    private EditText monthInput;
    private Button checkInButton;
    private Button checkOutButton;
    private Button refreshButton;
    private Button historyButton;

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
            showMessage(tr(
                    "කරුණාකර නැවත Login වන්න.",
                    "Please log in again."
            ));
            setButtonsEnabled(false);
        } else {
            loadTodayAttendance();
        }
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(25));
        scroll.addView(root);

        TextView heading = text(
                tr("දෛනික පැමිණීම", "DAILY ATTENDANCE"),
                22, NAVY, true);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(dp(12), dp(22), dp(12), dp(22));
        heading.setBackground(round(GOLD, 15));
        root.addView(heading);

        TextView date = text(
                new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.getDefault()
                ).format(new Date()),
                17, WHITE, true);
        date.setGravity(Gravity.CENTER);
        date.setPadding(0, dp(20), 0, dp(12));
        root.addView(date);

        LinearLayout statusCard = card(root);

        TextView statusTitle = text(
                tr("අද පැමිණීමේ තත්ත්වය", "TODAY'S ATTENDANCE STATUS"),
                16, GOLD, true);
        statusTitle.setGravity(Gravity.CENTER);
        statusCard.addView(statusTitle);

        todayStatus = text(
                tr("දත්ත ලබාගනිමින්...", "Loading..."),
                18, WHITE, true);
        todayStatus.setGravity(Gravity.CENTER);
        todayStatus.setPadding(0, dp(16), 0, dp(16));
        statusCard.addView(todayStatus);

        checkInText = addStatusLine(statusCard,
                tr("පැමිණි වේලාව", "Check In Time") + ": --");

        checkOutText = addStatusLine(statusCard,
                tr("පිටවූ වේලාව", "Check Out Time") + ": --");

        totalHoursText = addStatusLine(statusCard,
                tr("මුළු පැය ගණන", "Total Hours") + ": --");

        checkInButton = button(
                tr("පැමිණීම සටහන් කරන්න", "CHECK IN"),
                GREEN, WHITE);
        addButton(root, checkInButton);

        checkOutButton = button(
                tr("පිටවීම සටහන් කරන්න", "CHECK OUT"),
                RED, WHITE);
        addButton(root, checkOutButton);

        refreshButton = button(
                tr("තත්ත්වය යාවත්කාලීන කරන්න", "REFRESH STATUS"),
                GOLD, NAVY);
        addButton(root, refreshButton);

        messageText = text("", 13, WHITE, false);
        messageText.setGravity(Gravity.CENTER);
        messageText.setPadding(dp(5), dp(12), dp(5), dp(12));
        root.addView(messageText);

        TextView historyHeading = text(
                tr("මාසික පැමිණීමේ වාර්තාව", "MONTHLY ATTENDANCE HISTORY"),
                17, GOLD, true);
        historyHeading.setPadding(0, dp(25), 0, dp(12));
        root.addView(historyHeading);

        monthInput = new EditText(this);
        monthInput.setSingleLine(true);
        monthInput.setTextColor(WHITE);
        monthInput.setHintTextColor(Color.LTGRAY);
        monthInput.setHint("YYYY-MM");
        monthInput.setText(
                new SimpleDateFormat(
                        "yyyy-MM", Locale.getDefault()
                ).format(new Date()));
        monthInput.setPadding(dp(14), dp(10), dp(14), dp(10));
        monthInput.setBackground(round(Color.rgb(40, 53, 72), 10));
        root.addView(monthInput);

        historyButton = button(
                tr("වාර්තාව ලබාගන්න", "LOAD HISTORY"),
                GOLD, NAVY);
        addButton(root, historyButton);

        historyText = text(
                tr("වාර්තාවක් ලබාගෙන නැත.", "No history loaded."),
                14, WHITE, false);
        historyText.setPadding(dp(10), dp(15), dp(10), dp(15));
        root.addView(historyText);

        Button back = button(
                tr("ආපසු", "BACK"), Color.DKGRAY, WHITE);
        addButton(root, back);
        back.setOnClickListener(v -> finish());

        checkInButton.setOnClickListener(v ->
                performAttendanceAction("attendanceCheckIn"));

        checkOutButton.setOnClickListener(v ->
                performAttendanceAction("attendanceCheckOut"));

        refreshButton.setOnClickListener(v ->
                loadTodayAttendance());

        historyButton.setOnClickListener(v ->
                loadAttendanceHistory());

        setContentView(scroll);
    }

    private LinearLayout card(LinearLayout root) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackground(round(Color.rgb(35, 49, 68), 14));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(8), 0, dp(12));
        root.addView(card, p);

        return card;
    }

    private TextView addStatusLine(LinearLayout parent, String value) {
        TextView t = text(value, 14, WHITE, false);
        t.setPadding(0, dp(7), 0, dp(7));
        parent.addView(t);
        return t;
    }

    private void performAttendanceAction(String action) {
        setButtonsEnabled(false);
        messageText.setText(
                tr("කරුණාකර රැඳී සිටින්න...", "Please wait..."));

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", action);
                request.put("sessionToken", sessionToken);

                JSONObject result = postJson(request);

                runOnUiThread(() -> {
                    String message = result.optString(
                            "message",
                            tr("ප්‍රතිචාරයක් ලැබුණේ නැත.",
                               "No server message received.")
                    );

                    showMessage(message);

                    if (result.optBoolean("success", false)) {
                        loadTodayAttendance();
                    } else {
                        setButtonsEnabled(true);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showMessage(tr(
                            "Server සම්බන්ධතා දෝෂයක්. නැවත උත්සාහ කරන්න.",
                            "Server connection error. Please try again."
                    ));
                    setButtonsEnabled(true);
                });
            }
        });
    }

    private void loadTodayAttendance() {
        if (sessionToken.trim().isEmpty()) return;

        setButtonsEnabled(false);

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "getMyTodayAttendance");
                request.put("sessionToken", sessionToken);

                JSONObject result = postJson(request);

                runOnUiThread(() -> {
                    if (!result.optBoolean("success", false)) {
                        showMessage(result.optString(
                                "message", "Unable to load attendance."));
                        setButtonsEnabled(false);
                        return;
                    }

                    JSONObject attendance =
                            result.optJSONObject("attendance");

                    if (attendance == null ||
                            attendance == JSONObject.NULL) {
                        todayStatus.setText(
                                tr("අද පැමිණීම සටහන් කර නැත.",
                                   "NOT CHECKED IN TODAY"));
                        checkInText.setText(
                                tr("පැමිණි වේලාව", "Check In Time") + ": --");
                        checkOutText.setText(
                                tr("පිටවූ වේලාව", "Check Out Time") + ": --");
                        totalHoursText.setText(
                                tr("මුළු පැය ගණන", "Total Hours") + ": --");

                        checkInButton.setEnabled(true);
                        checkOutButton.setEnabled(false);
                    } else {
                        String inStatus = attendance.optString(
                                "checkInStatus", "");
                        String outStatus = attendance.optString(
                                "checkOutStatus", "");

                        checkInText.setText(
                                tr("පැමිණි වේලාව", "Check In Time") +
                                ": " + displayValue(
                                        attendance.optString("checkInTime", ""))
                        );

                        checkOutText.setText(
                                tr("පිටවූ වේලාව", "Check Out Time") +
                                ": " + displayValue(
                                        attendance.optString("checkOutTime", ""))
                        );

                        Object hours = attendance.opt("totalHours");
                        totalHoursText.setText(
                                tr("මුළු පැය ගණන", "Total Hours") +
                                ": " + (hours == null ||
                                        hours == JSONObject.NULL
                                        ? "--" : String.valueOf(hours))
                        );

                        if ("CHECKED_OUT".equalsIgnoreCase(outStatus)) {
                            todayStatus.setText(
                                    tr("අද සේවය අවසන් කර ඇත.",
                                       "CHECKED OUT"));
                            checkInButton.setEnabled(false);
                            checkOutButton.setEnabled(false);
                        } else if ("CHECKED_IN".equalsIgnoreCase(inStatus)) {
                            todayStatus.setText(
                                    tr("දැනට සේවයේ නිරතයි.",
                                       "CURRENTLY CHECKED IN"));
                            checkInButton.setEnabled(false);
                            checkOutButton.setEnabled(true);
                        } else {
                            todayStatus.setText(inStatus);
                            checkInButton.setEnabled(false);
                            checkOutButton.setEnabled(false);
                        }
                    }

                    refreshButton.setEnabled(true);
                    historyButton.setEnabled(true);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showMessage(tr(
                            "අද පැමිණීම ලබාගැනීමට නොහැකි විය.",
                            "Could not load today's attendance."
                    ));
                    setButtonsEnabled(true);
                });
            }
        });
    }

    private void loadAttendanceHistory() {
        String month = monthInput.getText().toString().trim();

        if (!month.matches("\\d{4}-\\d{2}")) {
            showMessage(tr(
                    "මාසය YYYY-MM ආකාරයෙන් ඇතුළත් කරන්න.",
                    "Enter month in YYYY-MM format."
            ));
            return;
        }

        historyButton.setEnabled(false);
        historyText.setText(
                tr("වාර්තාව ලබාගනිමින්...", "Loading history..."));

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "getMyAttendanceHistory");
                request.put("sessionToken", sessionToken);
                request.put("month", month);

                JSONObject result = postJson(request);

                runOnUiThread(() -> {
                    historyButton.setEnabled(true);

                    if (!result.optBoolean("success", false)) {
                        historyText.setText(result.optString(
                                "message", "Unable to load history."));
                        return;
                    }

                    JSONArray history = result.optJSONArray("history");

                    if (history == null || history.length() == 0) {
                        historyText.setText(
                                tr("මෙම මාසයට වාර්තා නැත.",
                                   "No attendance records for this month."));
                        return;
                    }

                    StringBuilder output = new StringBuilder();

                    for (int i = 0; i < history.length(); i++) {
                        JSONObject item = history.optJSONObject(i);
                        if (item == null) continue;

                        output.append("Date: ")
                                .append(item.optString("workDate", "--"))
                                .append("\n");

                        output.append("Check In: ")
                                .append(displayValue(
                                        item.optString("checkInTime", "")))
                                .append("\n");

                        output.append("Check Out: ")
                                .append(displayValue(
                                        item.optString("checkOutTime", "")))
                                .append("\n");

                        Object hours = item.opt("totalHours");
                        output.append("Total Hours: ")
                                .append(hours == null ||
                                        hours == JSONObject.NULL
                                        ? "--" : String.valueOf(hours))
                                .append("\n");

                        output.append("------------------------\n");
                    }

                    historyText.setText(output.toString());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    historyButton.setEnabled(true);
                    historyText.setText(
                            tr("වාර්තාව ලබාගැනීමේ දෝෂයකි.",
                               "Error loading attendance history."));
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

    private void setButtonsEnabled(boolean enabled) {
        if (checkInButton != null) checkInButton.setEnabled(enabled);
        if (checkOutButton != null) checkOutButton.setEnabled(enabled);
        if (refreshButton != null) refreshButton.setEnabled(enabled);
        if (historyButton != null) historyButton.setEnabled(enabled);
    }

    private String displayValue(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value;
    }

    private void showMessage(String value) {
        messageText.setText(value);
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }

    private String tr(String sinhala, String english) {
        return language.equals("si") ? sinhala : english;
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

    private void addButton(LinearLayout root, Button b) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(55));
        p.setMargins(0, dp(8), 0, dp(5));
        root.addView(b, p);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
