package com.shreeadvertising.workers;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private static final int RED = Color.rgb(215, 25, 32);
    private static final int YELLOW = Color.rgb(255, 212, 0);
    private static final int BLACK = Color.rgb(25, 25, 25);
    private static final int WHITE = Color.WHITE;

    private EditText mobileInput, passwordInput;
    private TextView titleText, mobileLabel, passwordLabel;
    private Button sinhalaButton, englishButton, loginButton, registerButton;
    private SharedPreferences preferences;
    private String language = "si";

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE);
        language = preferences.getString("language", "si");
        getWindow().setStatusBarColor(RED);
        getWindow().setNavigationBarColor(BLACK);
        buildLoginScreen();
    }

    private void buildLoginScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(WHITE);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(dp(25), dp(35), dp(25), dp(30));
        mainLayout.setBackgroundColor(WHITE);
        scrollView.addView(mainLayout);
        setContentView(scrollView);

        TextView logo = new TextView(this);
        logo.setText("ශ්‍රී");
        logo.setTextColor(RED);
        logo.setTextSize(38);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        mainLayout.addView(logo, matchWrap());

        TextView workers = new TextView(this);
        workers.setText("ADVERTISING WORKERS");
        workers.setTextColor(BLACK);
        workers.setTextSize(19);
        workers.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        workers.setGravity(Gravity.CENTER);
        mainLayout.addView(workers, matchWrap());

        TextView system = new TextView(this);
        system.setText("WORKERS MANAGEMENT SYSTEM");
        system.setTextColor(Color.GRAY);
        system.setTextSize(12);
        system.setGravity(Gravity.CENTER);
        system.setPadding(0, dp(5), 0, dp(25));
        mainLayout.addView(system, matchWrap());

        TextView languageTitle = new TextView(this);
        languageTitle.setText("SELECT LANGUAGE / භාෂාව තෝරන්න");
        languageTitle.setTextColor(BLACK);
        languageTitle.setTextSize(14);
        languageTitle.setGravity(Gravity.CENTER);
        languageTitle.setPadding(0, dp(5), 0, dp(12));
        mainLayout.addView(languageTitle, matchWrap());

        LinearLayout languageRow = new LinearLayout(this);
        languageRow.setOrientation(LinearLayout.HORIZONTAL);
        languageRow.setGravity(Gravity.CENTER);
        mainLayout.addView(languageRow, matchWrap());

        sinhalaButton = createButton("සිංහල", YELLOW, BLACK);
        englishButton = createButton("ENGLISH", YELLOW, BLACK);
        LinearLayout.LayoutParams languageParams =
            new LinearLayout.LayoutParams(0, dp(48), 1);
        languageParams.setMargins(dp(5), 0, dp(5), 0);
        languageRow.addView(sinhalaButton, languageParams);
        languageRow.addView(englishButton, languageParams);

        titleText = new TextView(this);
        titleText.setTextColor(BLACK);
        titleText.setTextSize(25);
        titleText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, dp(35), 0, dp(25));
        mainLayout.addView(titleText, matchWrap());

        mobileLabel = createLabel("");
        mainLayout.addView(mobileLabel, matchWrap());

        mobileInput = new EditText(this);
        mobileInput.setSingleLine(true);
        mobileInput.setInputType(3);
        mobileInput.setTextSize(17);
        mobileInput.setPadding(dp(15), 0, dp(15), 0);
        mobileInput.setHint("07XXXXXXXX");
        mobileInput.setBackground(inputBackground());
        LinearLayout.LayoutParams inputParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(55));
        inputParams.setMargins(0, dp(5), 0, dp(18));
        mainLayout.addView(mobileInput, inputParams);

        passwordLabel = createLabel("");
        mainLayout.addView(passwordLabel, matchWrap());

        passwordInput = new EditText(this);
        passwordInput.setSingleLine(true);
        passwordInput.setTextSize(17);
        passwordInput.setInputType(129);
        passwordInput.setPadding(dp(15), 0, dp(15), 0);
        passwordInput.setHint("Password");
        passwordInput.setBackground(inputBackground());
        LinearLayout.LayoutParams passwordParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(55));
        passwordParams.setMargins(0, dp(5), 0, dp(25));
        mainLayout.addView(passwordInput, passwordParams);

        loginButton = createButton("", RED, WHITE);
        loginButton.setTextSize(18);
        loginButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams loginParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(55));
        loginParams.setMargins(0, dp(5), 0, dp(12));
        mainLayout.addView(loginButton, loginParams);

        registerButton = createButton("", YELLOW, BLACK);
        registerButton.setTextSize(17);
        registerButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams registerParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(55));
        registerParams.setMargins(0, dp(5), 0, dp(25));
        mainLayout.addView(registerButton, registerParams);

        TextView footer = new TextView(this);
        footer.setText("SmartSolution PVT Ltd\nDeveloped by Menaka Nuwan kumarasinghe\nWhatsApp: 0769230208");
        footer.setTextColor(Color.GRAY);
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(20), 0, 0);
        mainLayout.addView(footer, matchWrap());

        sinhalaButton.setOnClickListener(v -> changeLanguage("si"));
        englishButton.setOnClickListener(v -> changeLanguage("en"));
        loginButton.setOnClickListener(v -> loginWorker());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,
                RegistrationActivity.class);
            startActivity(intent);
        });
        updateLanguage();
    }

    private void changeLanguage(String selectedLanguage) {
        language = selectedLanguage;
        preferences.edit().putString("language", language).apply();
        updateLanguage();
    }

    private void updateLanguage() {
        boolean sinhala = language.equals("si");
        titleText.setText(sinhala ? "සේවක පිවිසුම" : "WORKER LOGIN");
        mobileLabel.setText(sinhala ? "දුරකථන අංකය" : "MOBILE NUMBER");
        passwordLabel.setText(sinhala ? "මුරපදය" : "PASSWORD");
        loginButton.setText(sinhala ? "පිවිසෙන්න" : "LOGIN");
        registerButton.setText(sinhala
            ? "නව සේවකයෙකු ලෙස ලියාපදිංචි වන්න"
            : "REGISTER AS A NEW WORKER");
        mobileInput.setHint(sinhala
            ? "ඔබගේ දුරකථන අංකය ඇතුළත් කරන්න"
            : "Enter your mobile number");
        passwordInput.setHint(sinhala
            ? "මුරපදය ඇතුළත් කරන්න"
            : "Enter your password");
        sinhalaButton.setAlpha(sinhala ? 1f : 0.55f);
        englishButton.setAlpha(sinhala ? 0.55f : 1f);
    }

    private void loginWorker() {
        String mobile = mobileInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (mobile.isEmpty()) {
            showMessage(language.equals("si")
                ? "දුරකථන අංකය ඇතුළත් කරන්න."
                : "Please enter your mobile number.");
            return;
        }

        if (password.isEmpty()) {
            showMessage(language.equals("si")
                ? "මුරපදය ඇතුළත් කරන්න."
                : "Please enter your password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText(language.equals("si")
            ? "පරීක්ෂා කරමින්..."
            : "PLEASE WAIT...");

        executor.execute(() -> {
            HttpURLConnection connection = null;
            HttpURLConnection redirectConnection = null;

            try {
                JSONObject request = new JSONObject();
                request.put("action", "workerLogin");
                request.put("mobile", mobile);
                request.put("password", password);

                connection = (HttpURLConnection)
                    new URL(API_URL).openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty(
                    "Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");

                byte[] body = request.toString()
                    .getBytes(StandardCharsets.UTF_8);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                    output.flush();
                }

                int responseCode = connection.getResponseCode();
                String responseText;

                if (responseCode == 301 || responseCode == 302 ||
                    responseCode == 303 || responseCode == 307 ||
                    responseCode == 308) {

                    String redirectUrl =
                        connection.getHeaderField("Location");

                    if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
                        throw new Exception("Redirect URL missing.");
                    }

                    redirectConnection = (HttpURLConnection)
                        new URL(redirectUrl).openConnection();

                    redirectConnection.setRequestMethod("GET");
                    redirectConnection.setConnectTimeout(20000);
                    redirectConnection.setReadTimeout(30000);

                    responseText = readResponse(
                        redirectConnection,
                        redirectConnection.getResponseCode());
                } else {
                    responseText = readResponse(connection, responseCode);
                }

                JSONObject result = new JSONObject(responseText);
                runOnUiThread(() -> handleLoginResult(result));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showMessage(language.equals("si")
                        ? "සම්බන්ධතාවයේ දෝෂයක්. නැවත උත්සාහ කරන්න."
                        : "Connection error. Please try again.");
                    resetLoginButton();
                });
            } finally {
                if (redirectConnection != null) {
                    redirectConnection.disconnect();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(
        HttpURLConnection connection, int responseCode) throws Exception {

        InputStream stream = (responseCode >= 200 && responseCode < 400)
            ? connection.getInputStream()
            : connection.getErrorStream();

        if (stream == null) {
            throw new Exception("Server response unavailable.");
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode < 200 || responseCode >= 400) {
            throw new Exception("HTTP error: " + responseCode);
        }

        return response.toString();
    }

    private void handleLoginResult(JSONObject result) {
        boolean success = result.optBoolean("success", false);

        if (success) {
            String token = result.optString("sessionToken", "");

            if (token.isEmpty()) {
                showMessage(language.equals("si")
                    ? "Session Token එක ලැබුණේ නැහැ. නැවත උත්සාහ කරන්න."
                    : "Session token was not received. Please try again.");
                resetLoginButton();
                return;
            }

            preferences.edit()
                .putString("workerSessionToken", token)
                .apply();

            showMessage(result.optString("message",
                language.equals("si")
                    ? "පිවිසුම සාර්ථකයි."
                    : "Login successful."));

            Intent intent = new Intent(this, WorkerHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        showMessage(result.optString("message",
            language.equals("si")
                ? "පිවිසීම අසාර්ථකයි."
                : "Login failed."));

        resetLoginButton();
    }

    private void resetLoginButton() {
        loginButton.setEnabled(true);
        loginButton.setText(language.equals("si")
            ? "පිවිසෙන්න"
            : "LOGIN");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(BLACK);
        label.setTextSize(15);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return label;
    }

    private Button createButton(
        String text, int backgroundColor, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(textColor);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setBackground(roundBackground(backgroundColor, 12));
        return button;
    }

    private GradientDrawable roundBackground(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable inputBackground() {
        GradientDrawable drawable =
            roundBackground(Color.rgb(248, 248, 248), 10);
        drawable.setStroke(dp(1), Color.LTGRAY);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources()
            .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
