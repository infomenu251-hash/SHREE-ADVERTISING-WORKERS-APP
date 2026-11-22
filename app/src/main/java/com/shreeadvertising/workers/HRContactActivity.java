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
import android.widget.Toast;

public class HRContactActivity extends Activity {

    private final int NAVY = Color.rgb(16, 27, 45);
    private final int GOLD = Color.rgb(214, 173, 85);
    private final int WHITE = Color.WHITE;
    private String language = "si";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE)
                .getString("language", "si");

        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(25));
        scroll.addView(root);

        TextView heading = text(
                language.equals("si") ? "HR / කළමනාකරණ සම්බන්ධතා" : "HR / MANAGEMENT CONTACT",
                21, NAVY, true);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(dp(12), dp(22), dp(12), dp(22));
        heading.setBackground(round(GOLD, 16));
        root.addView(heading);

        TextView subtitle = text(
                language.equals("si")
                        ? "ශ්‍රී ADVERTISING WORKERS"
                        : "ශ්‍රී ADVERTISING WORKERS",
                17, WHITE, true);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(25), 0, dp(18));
        root.addView(subtitle);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(20), dp(18), dp(20));
        card.setBackground(round(WHITE, 16));

        TextView title = text(
                language.equals("si") ? "මානව සම්පත් / කළමනාකරණ අංශය"
                        : "HUMAN RESOURCES / MANAGEMENT",
                17, NAVY, true);
        title.setGravity(Gravity.CENTER);
        card.addView(title);

        TextView phone = text(
                language.equals("si") ? "දුරකථන අංකය: ලබාගනිමින් පවතී"
                        : "Phone Number: Not configured",
                15, Color.DKGRAY, false);
        phone.setGravity(Gravity.CENTER);
        phone.setPadding(0, dp(18), 0, dp(18));
        card.addView(phone);

        Button call = button(
                language.equals("si") ? "දුරකථන ඇමතුමක් ගන්න" : "CALL HR / MANAGEMENT",
                Color.rgb(32, 145, 85), WHITE);
        card.addView(call);

        Button whatsapp = button(
                language.equals("si") ? "WhatsApp පණිවිඩයක් යවන්න" : "MESSAGE ON WHATSAPP",
                Color.rgb(37, 170, 95), WHITE);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        wp.setMargins(0, dp(12), 0, 0);
        card.addView(whatsapp, wp);

        root.addView(card);

        call.setOnClickListener(v -> showNotConfigured());
        whatsapp.setOnClickListener(v -> showNotConfigured());

        TextView info = text(
                language.equals("si")
                        ? "HR සම්බන්ධතා අංක සහ තොරතුරු Admin Panel එකෙන් සකස් කර සේවකයන්ට ලබාදෙනු ඇත."
                        : "HR contact numbers and details will be configured through the Admin Panel and made available to employees.",
                14, WHITE, false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(dp(8), dp(25), dp(8), dp(15));
        root.addView(info);

        Button back = button(
                language.equals("si") ? "ආපසු" : "BACK",
                GOLD, NAVY);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(55));
        bp.setMargins(0, dp(15), 0, 0);
        root.addView(back, bp);
        back.setOnClickListener(v -> finish());

        setContentView(scroll);
    }

    private void showNotConfigured() {
        Toast.makeText(this,
                language.equals("si")
                        ? "HR දුරකථන අංකය තවම සකස් කර නැත."
                        : "HR contact number has not been configured yet.",
                Toast.LENGTH_LONG).show();
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
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
