package com.shreeadvertising.workers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int RED = Color.rgb(215, 25, 32);
    private static final int YELLOW = Color.rgb(255, 212, 0);
    private static final int WHITE = Color.WHITE;

    private static final long SPLASH_DURATION = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ProgressBar loadingBar;
    private TextView loadingText;
    private long startTime;

    private final Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = SystemClock.elapsedRealtime() - startTime;
            int progress = (int) Math.min(
                100, (elapsed * 100) / SPLASH_DURATION
            );

            loadingBar.setProgress(progress);
            loadingText.setText("LOADING... " + progress + "%");

            if (elapsed >= SPLASH_DURATION) {
                openMainScreen();
            } else {
                handler.postDelayed(this, 50);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(RED);
        getWindow().setNavigationBarColor(RED);

        buildSplashScreen();

        startTime = SystemClock.elapsedRealtime();
        handler.post(loadingRunnable);
    }

    private void buildSplashScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(RED);
        root.setPadding(dp(24), dp(20), dp(24), dp(22));

        setContentView(root);

        View topSpace = new View(this);
        root.addView(topSpace,
            new LinearLayout.LayoutParams(1, 0, 1));

        LinearLayout centerLayout = new LinearLayout(this);
        centerLayout.setOrientation(LinearLayout.VERTICAL);
        centerLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        root.addView(centerLayout,
            new LinearLayout.LayoutParams(
                -1, -2
            ));

        ImageView logo = new ImageView(this);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(WHITE);
        circle.setStroke(dp(4), YELLOW);

        logo.setBackground(circle);
        logo.setClipToOutline(true);
        logo.setPadding(dp(10), dp(10), dp(10), dp(10));
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setImageResource(R.drawable.shri_logo);

        LinearLayout.LayoutParams logoParams =
            new LinearLayout.LayoutParams(dp(145), dp(145));
        logoParams.gravity = Gravity.CENTER;
        logoParams.bottomMargin = dp(22);

        centerLayout.addView(logo, logoParams);

        TextView companyName = new TextView(this);
        companyName.setText("ශ්‍රී ADVERTISING");
        companyName.setTextColor(WHITE);
        companyName.setTextSize(27);
        companyName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        companyName.setGravity(Gravity.CENTER);

        centerLayout.addView(companyName,
            new LinearLayout.LayoutParams(-1, -2));

        TextView appName = new TextView(this);
        appName.setText("WORKERS MANAGEMENT SYSTEM");
        appName.setTextColor(WHITE);
        appName.setTextSize(13);
        appName.setGravity(Gravity.CENTER);
        appName.setPadding(0, dp(8), 0, 0);

        centerLayout.addView(appName,
            new LinearLayout.LayoutParams(-1, -2));

        View middleSpace = new View(this);
        root.addView(middleSpace,
            new LinearLayout.LayoutParams(1, 0, 1));

        loadingText = new TextView(this);
        loadingText.setText("LOADING... 0%");
        loadingText.setTextColor(WHITE);
        loadingText.setTextSize(14);
        loadingText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(0, 0, 0, dp(10));

        root.addView(loadingText,
            new LinearLayout.LayoutParams(-1, -2));

        loadingBar = new ProgressBar(
            this, null, android.R.attr.progressBarStyleHorizontal
        );

        loadingBar.setMax(100);
        loadingBar.setProgress(0);
        loadingBar.setProgressTintList(
            android.content.res.ColorStateList.valueOf(YELLOW)
        );
        loadingBar.setProgressBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(150, 15, 20)
            )
        );

        root.addView(loadingBar,
            new LinearLayout.LayoutParams(-1, dp(12)));

        TextView powered = new TextView(this);
        powered.setText("POWERED BY");
        powered.setTextColor(YELLOW);
        powered.setTextSize(12);
        powered.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        powered.setGravity(Gravity.CENTER);
        powered.setPadding(0, dp(22), 0, dp(5));

        root.addView(powered,
            new LinearLayout.LayoutParams(-1, -2));

        TextView organization = new TextView(this);
        organization.setText("SmartSolution PVT Ltd");
        organization.setTextColor(WHITE);
        organization.setTextSize(17);
        organization.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        organization.setGravity(Gravity.CENTER);

        root.addView(organization,
            new LinearLayout.LayoutParams(-1, -2));

        TextView developer = new TextView(this);
        developer.setText(
            "Developed by\nMenaka Nuwan kumarasinghe"
        );
        developer.setTextColor(WHITE);
        developer.setTextSize(12);
        developer.setGravity(Gravity.CENTER);
        developer.setPadding(0, dp(7), 0, dp(5));

        root.addView(developer,
            new LinearLayout.LayoutParams(-1, -2));

        TextView contact = new TextView(this);
        contact.setText("WhatsApp: 0769230208");
        contact.setTextColor(WHITE);
        contact.setTextSize(13);
        contact.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        contact.setGravity(Gravity.CENTER);

        root.addView(contact,
            new LinearLayout.LayoutParams(-1, -2));
    }

    private void openMainScreen() {
        handler.removeCallbacks(loadingRunnable);

        Intent intent = new Intent(
            SplashActivity.this, MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return (int) (value *
            getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(loadingRunnable);
        super.onDestroy();
    }
}
