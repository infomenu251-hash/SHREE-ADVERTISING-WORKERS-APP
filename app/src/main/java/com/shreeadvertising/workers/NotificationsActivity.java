package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsActivity extends Activity {

    private static final String API_URL =
            "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private final int WHITE = Color.WHITE;

    private String token;
    private boolean sinhala;

    private TextView statusText;
    private LinearLayout notificationList;
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences("WorkerAppSettings", MODE_PRIVATE);

        token = prefs.getString("workerSessionToken", "");
        sinhala = "si".equalsIgnoreCase(
                prefs.getString("language", "si"));

        buildUI();

        if (token.isEmpty()) {
            statusText.setText(sinhala ?
                    "නැවත Login වන්න." :
                    "Please log in again.");
            refreshButton.setEnabled(false);
        } else {
            loadNotifications();
        }
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(22),dp(18),dp(25));
        scroll.addView(root);

        TextView title = makeText(
                sinhala ? "දැනුම්දීම්" : "NOTIFICATIONS",
                24,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full());

        TextView subtitle = makeText(
                sinhala ? "පරිපාලකගෙන් ලැබෙන පණිවිඩ" :
                        "MESSAGES FROM ADMIN",
                13,WHITE,false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(7),0,dp(18));
        root.addView(subtitle,full());

        refreshButton = makeButton(
                sinhala ? "නැවත ලබාගන්න" : "REFRESH",
                GOLD,NAVY);
        root.addView(refreshButton,buttonParams());
        refreshButton.setOnClickListener(v -> loadNotifications());

        statusText = makeText("",14,GOLD,false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0,dp(14),0,dp(14));
        root.addView(statusText,full());

        notificationList = new LinearLayout(this);
        notificationList.setOrientation(LinearLayout.VERTICAL);
        root.addView(notificationList,full());

        setContentView(scroll);
    }

    private TextView makeText(String value,int size,int color,
                              boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null,Typeface.BOLD);
        return t;
    }

    private Button makeButton(String value,int bg,int fg) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(14);
        b.setTypeface(null,Typeface.BOLD);
        b.setTextColor(fg);
        b.setAllCaps(false);
        b.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(bg));
        return b;
    }

    private LinearLayout.LayoutParams full() {
        return new LinearLayout.LayoutParams(-1,-2);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,dp(52));
        p.topMargin = dp(6);
        p.bottomMargin = dp(6);
        return p;
    }

    private int dp(int value) {
        return (int)(value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private void loadNotifications() {
        refreshButton.setEnabled(false);
        statusText.setText(sinhala ?
                "දැනුම්දීම් ලබාගනිමින්..." :
                "Loading notifications...");

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action","getMyNotifications");
                request.put("sessionToken",token);

                JSONObject result = post(request);

                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);
                    showNotifications(result);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    refreshButton.setEnabled(true);
                    statusText.setText(sinhala ?
                            "දැනුම්දීම් ලබාගැනීමට නොහැකි වුණා." :
                            "Could not load notifications.");
                });
            }
        });
    }

    private void showNotifications(JSONObject result) {
        notificationList.removeAllViews();

        if (!result.optBoolean("success",false)) {
            statusText.setText(result.optString("message",
                    sinhala ? "දත්ත ලබාගැනීම අසාර්ථකයි." :
                            "Request failed."));
            return;
        }

        JSONArray items = result.optJSONArray("notifications");

        if (items == null || items.length() == 0) {
            statusText.setText(sinhala ?
                    "දැනුම්දීම් කිසිවක් නැහැ." :
                    "No notifications available.");
            return;
        }

        statusText.setText(sinhala ?
                "දැනුම්දීම් ගණන: " + items.length() :
                "Total notifications: " + items.length());

        for (int i=0; i<items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null) addNotificationCard(item);
        }
    }

    private void addNotificationCard(JSONObject item) {
        String id = item.optString("notificationID","");
        String title = item.optString("title","");
        String message = item.optString("message","");
        String type = item.optString("type","");
        String createdAt = item.optString("createdAt","");
        String currentStatus = item.optString("status","");

        boolean alreadyRead =
                currentStatus.equalsIgnoreCase("READ");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15),dp(14),dp(15),dp(14));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(37,51,70));
        background.setCornerRadius(dp(13));
        background.setStroke(dp(1),
                alreadyRead ? Color.GRAY : GOLD);
        card.setBackground(background);

        LinearLayout.LayoutParams cp = full();
        cp.bottomMargin = dp(13);
        notificationList.addView(card,cp);

        TextView heading = makeText(
                title.isEmpty() ?
                        (sinhala ? "දැනුම්දීම" : "Notification") : title,
                18,GOLD,true);
        card.addView(heading,full());

        if (!type.isEmpty()) {
            TextView typeText = makeText(
                    (sinhala ? "වර්ගය: " : "Type: ") + type,
                    12,Color.LTGRAY,false);
            typeText.setPadding(0,dp(5),0,dp(5));
            card.addView(typeText,full());
        }

        TextView body = makeText(message,15,WHITE,false);
        body.setPadding(0,dp(8),0,dp(8));
        card.addView(body,full());

        if (!createdAt.isEmpty()) {
            TextView date = makeText(
                    (sinhala ? "දිනය: " : "Date: ") + createdAt,
                    12,Color.LTGRAY,false);
            date.setPadding(0,dp(4),0,dp(4));
            card.addView(date,full());
        }

        TextView state = makeText(
                alreadyRead ?
                        (sinhala ? "කියවා ඇත" : "READ") :
                        (sinhala ? "කියවා නැත" : "UNREAD"),
                13,alreadyRead ? Color.LTGRAY : GOLD,true);
        state.setPadding(0,dp(7),0,dp(5));
        card.addView(state,full());

        if (!alreadyRead && !id.isEmpty()) {
            Button readButton = makeButton(
                    sinhala ? "කියවූ බව සලකුණු කරන්න" :
                            "MARK AS READ",
                    GOLD,NAVY);

            LinearLayout.LayoutParams rp = buttonParams();
            rp.topMargin = dp(8);
            card.addView(readButton,rp);

            readButton.setOnClickListener(v ->
                    markAsRead(id,readButton));
        }
    }

    private void markAsRead(String notificationID,Button button) {
        button.setEnabled(false);
        button.setText(sinhala ? "යාවත්කාලීන කරමින්..." : "UPDATING...");

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action","markNotificationRead");
                request.put("sessionToken",token);
                request.put("notificationID",notificationID);

                JSONObject result = post(request);

                runOnUiThread(() -> {
                    if (result.optBoolean("success",false)) {
                        statusText.setText(result.optString("message",
                                sinhala ? "කියවූ බව සලකුණු කළා." :
                                        "Notification marked as read."));
                        loadNotifications();
                    } else {
                        button.setEnabled(true);
                        button.setText(sinhala ?
                                "කියවූ බව සලකුණු කරන්න" :
                                "MARK AS READ");
                        statusText.setText(result.optString("message",
                                sinhala ? "යාවත්කාලීන කිරීමට නොහැකි වුණා." :
                                        "Could not update notification."));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    button.setEnabled(true);
                    button.setText(sinhala ?
                            "කියවූ බව සලකුණු කරන්න" :
                            "MARK AS READ");
                    statusText.setText(sinhala ?
                            "සම්බන්ධතාවයේ දෝෂයක්." :
                            "Connection error.");
                });
            }
        });
    }

    private JSONObject post(JSONObject data) throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection)new URL(API_URL).openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty(
                    "Content-Type","application/json; charset=UTF-8");

            try (OutputStream out = connection.getOutputStream()) {
                out.write(data.toString().getBytes("UTF-8"));
            }

            int code = connection.getResponseCode();

            InputStream input = (code >= 200 && code < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (input == null) {
                throw new Exception("Empty response: " + code);
            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input,"UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            return new JSONObject(response.toString());

        } finally {
            connection.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
