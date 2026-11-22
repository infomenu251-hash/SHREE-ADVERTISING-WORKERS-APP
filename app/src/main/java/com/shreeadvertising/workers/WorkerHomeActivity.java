
package com.shreeadvertising.workers;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WorkerHomeActivity extends Activity {

    private final int RED = Color.rgb(215, 25, 32);
    private final int YELLOW = Color.rgb(255, 212, 0);
    private final int BLACK = Color.rgb(25, 25, 25);
    private final int WHITE = Color.WHITE;

    private static final String API_URL =
            "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private String language = "si";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE)
                .getString("language", "si");

        buildDashboard();
    }

    private void buildDashboard() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(248, 248, 248));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(25));

        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(dp(15), dp(22), dp(15), dp(22));
        header.setBackground(round(RED, 18));

        TextView logo = text("ශ්‍රී", 38, WHITE, true);
        header.addView(logo);

        TextView company = text("ADVERTISING WORKERS", 19, WHITE, true);
        header.addView(company);

        TextView welcome = text(
                language.equals("si") ? "සේවක පාලක පුවරුව" : "WORKER DASHBOARD",
                15, YELLOW, true);
        welcome.setPadding(0, dp(10), 0, 0);
        header.addView(welcome);

        root.addView(header);

        TextView section = text(
                language.equals("si") ? "මගේ සේවා පහසුකම්" : "MY SERVICES",
                18, BLACK, true);
        section.setPadding(dp(3), dp(25), 0, dp(12));
        root.addView(section);

        addMenu(root, "🕒", "Attendance", "පැමිණීම / පිටවීම");
        addMenu(root, "👥", "Today's Workers", "අද සේවයට පැමිණි අය");
        addMenu(root, "📝", "Daily Work", "දෛනික වැඩ වාර්තා");
        addMenu(root, "📊", "Day End Report", "දවසේ අවසාන වාර්තාව");
        addMenu(root, "📅", "Appointments", "හමුවීම්");
        addMenu(root, "👥", "My Clients", "මගේ පාරිභෝගිකයන්");
        addMenu(root, "💰", "My Salary", "මගේ වැටුප");
        addMenu(root, "🔔", "Notifications", "දැනුම්දීම්");
        addMenu(root, "📄", "Monthly Reports", "මාසික වාර්තා");
        addMenu(root, "👤", "My Profile", "මගේ තොරතුරු");
        addMenu(root, "☎", "HR Contact", "කළමනාකාරීත්වය අමතන්න");

        Button logout = new Button(this);
        logout.setText(language.equals("si") ? "ඉවත් වන්න" : "LOG OUT");
        logout.setTextColor(WHITE);
        logout.setTextSize(16);
        logout.setTypeface(null, Typeface.BOLD);
        logout.setBackground(round(RED, 14));

        LinearLayout.LayoutParams logoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        logoutParams.setMargins(0, dp(25), 0, dp(12));
        root.addView(logout, logoutParams);

        logout.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(language.equals("si") ? "ඉවත් වීම" : "Log out")
                .setMessage(language.equals("si")
                        ? "ඔබට App එකෙන් ඉවත් වීමට අවශ්‍යද?"
                        : "Do you want to log out?")
                .setNegativeButton(language.equals("si") ? "නැහැ" : "Cancel", null)
                .setPositiveButton(language.equals("si") ? "ඔව්" : "Yes",
                        (dialog, which) -> performLogout())
                .show());

        TextView footer = text(
                "SmartSolution PVT Ltd\nDeveloped by Menaka Nuwan kumarasinghe",
                12, Color.GRAY, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(12), 0, dp(12));
        root.addView(footer);

        setContentView(scroll);
    }

    private void performLogout() {

        String token = getSharedPreferences(
                "WorkerAppSettings", MODE_PRIVATE)
                .getString("workerSessionToken", "");

        getSharedPreferences("WorkerAppSettings", MODE_PRIVATE)
                .edit()
                .remove("workerSessionToken")
                .apply();

        if (token != null && !token.trim().isEmpty()) {
            Thread logoutThread = new Thread(() -> {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(API_URL);
                    connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setDoOutput(true);
                    connection.setRequestProperty(
                            "Content-Type", "application/json; charset=UTF-8");

                    JSONObject request = new JSONObject();
                    request.put("action", "workerLogout");
                    request.put("sessionToken", token);

                    byte[] body = request.toString()
                            .getBytes(StandardCharsets.UTF_8);

                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(body);
                        output.flush();
                    }

                    connection.getResponseCode();

                } catch (Exception e) {
                    android.util.Log.e(
                            "WorkerLogout",
                            "Server logout request failed", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });

            logoutThread.start();
        }

        finish();
    }

    private void addMenu(LinearLayout root, String icon,
                         String english, String sinhala) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(15), dp(13), dp(12), dp(13));
        item.setBackground(round(WHITE, 14));
        item.setElevation(dp(2));

        TextView symbol = text(icon, 27, RED, true);
        symbol.setGravity(Gravity.CENTER);
        item.addView(symbol, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(12), 0, 0, 0);

        labels.addView(text(english, 16, BLACK, true));
        labels.addView(text(sinhala, 13, Color.DKGRAY, false));

        item.addView(labels, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView arrow = text("›", 30, RED, true);
        item.addView(arrow);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(76));
        params.setMargins(0, 0, 0, dp(12));
        root.addView(item, params);

        item.setOnClickListener(v -> {
            if (english.equals("Today's Workers")) {
                startActivity(new android.content.Intent(
                        this, TodayWorkersActivity.class));
            } else if (english.equals("Attendance")) {
                startActivity(new android.content.Intent(
                        this, AttendanceActivity.class));
            } else if (english.equals("Daily Work")) {
                startActivity(new android.content.Intent(
                        this, DailyWorkActivity.class));
            } else if (english.equals("Day End Report")) {
                startActivity(new android.content.Intent(
                        this, DayEndActivity.class));
            } else if (english.equals("Appointments")) {
                startActivity(new android.content.Intent(
                        this, AppointmentsActivity.class));
            } else if (english.equals("My Clients")) {
                startActivity(new android.content.Intent(
                        this, MyClientsActivity.class));
            } else if (english.equals("My Salary")) {
                startActivity(new android.content.Intent(
                        this, MySalaryActivity.class));
            } else if (english.equals("Notifications")) {
                startActivity(new android.content.Intent(
                        this, NotificationsActivity.class));
            } else if (english.equals("Monthly Reports")) {
                startActivity(new android.content.Intent(
                        this, MonthlyReportsActivity.class));
            } else if (english.equals("My Profile")) {
                startActivity(new android.content.Intent(
                        this, MyProfileActivity.class));
            } else if (english.equals("HR Contact")) {
                startActivity(new android.content.Intent(
                        this, HRContactActivity.class));
            }
        });
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }
}
