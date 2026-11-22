
package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyProfileActivity extends Activity {

    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private static final int RED = Color.rgb(215, 25, 32);
    private static final int BLACK = Color.rgb(25, 25, 25);
    private static final int WHITE = Color.WHITE;
    private static final int GRAY = Color.rgb(110, 110, 110);

    private String language = "si";
    private String sessionToken = "";

    private LinearLayout mainLayout;
    private LinearLayout profileCard;
    private ImageView profileImage;
    private TextView statusText;
    private Button refreshButton;

    private TextView employeeIDValue;
    private TextView fullNameValue;
    private TextView mobileValue;
    private TextView addressValue;
    private TextView genderValue;
    private TextView dobValue;
    private TextView positionValue;
    private TextView accountStatusValue;
    private TextView registrationDateValue;

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
        new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = getSharedPreferences(
            "WorkerAppSettings",
            MODE_PRIVATE
        ).getString("language", "si");

        sessionToken = getSharedPreferences(
            "WorkerAppSettings",
            MODE_PRIVATE
        ).getString("workerSessionToken", "");

        buildScreen();

        if (sessionToken.isEmpty()) {
            showStatus(
                isSinhala()
                    ? "කරුණාකර නැවත Login වන්න."
                    : "Please login again."
            );
            refreshButton.setEnabled(false);
        } else {
            loadProfile();
        }
    }


    private boolean isSinhala() {
        return "si".equals(language);
    }


    private void buildScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(
            Color.rgb(245, 246, 248)
        );

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(18), dp(20), dp(18), dp(24));

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        TextView title = new TextView(this);
        title.setText(
            isSinhala() ? "මගේ සේවක තොරතුරු" : "MY PROFILE"
        );
        title.setTextColor(BLACK);
        title.setTextSize(23);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(
            title,
            new LinearLayout.LayoutParams(
                -1, dp(55)
            )
        );

        TextView subtitle = new TextView(this);
        subtitle.setText(
            isSinhala()
                ? "ඔබගේ පුද්ගලික සේවක තොරතුරු"
                : "Your personal employee information"
        );
        subtitle.setTextColor(GRAY);
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        mainLayout.addView(
            subtitle,
            new LinearLayout.LayoutParams(
                -1, dp(32)
            )
        );

        profileCard = new LinearLayout(this);
        profileCard.setOrientation(LinearLayout.VERTICAL);
        profileCard.setGravity(Gravity.CENTER_HORIZONTAL);
        profileCard.setPadding(dp(16), dp(20), dp(16), dp(20));
        profileCard.setBackground(round(
            WHITE, dp(16), Color.rgb(225, 225, 225)
        ));

        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(-1, -2);

        cardParams.setMargins(0, dp(12), 0, dp(14));
        mainLayout.addView(profileCard, cardParams);

        profileImage = new ImageView(this);
        profileImage.setImageDrawable(
            round(RED, dp(60), RED)
        );
        profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LinearLayout.LayoutParams imageParams =
            new LinearLayout.LayoutParams(dp(115), dp(115));

        imageParams.gravity = Gravity.CENTER;
        imageParams.bottomMargin = dp(12);

        profileCard.addView(profileImage, imageParams);

        TextView photoLabel = new TextView(this);
        photoLabel.setText(
            isSinhala() ? "සේවක ඡායාරූපය" : "PROFILE PHOTO"
        );
        photoLabel.setTextColor(GRAY);
        photoLabel.setTextSize(12);
        photoLabel.setGravity(Gravity.CENTER);
        photoLabel.setPadding(0, 0, 0, dp(15));
        profileCard.addView(photoLabel);

        employeeIDValue = addInfo(
            isSinhala() ? "සේවක අංකය" : "EMPLOYEE ID"
        );

        fullNameValue = addInfo(
            isSinhala() ? "සම්පූර්ණ නම" : "FULL NAME"
        );

        mobileValue = addInfo(
            isSinhala() ? "දුරකථන අංකය" : "MOBILE NUMBER"
        );

        addressValue = addInfo(
            isSinhala() ? "ලිපිනය" : "ADDRESS"
        );

        genderValue = addInfo(
            isSinhala() ? "ස්ත්‍රී / පුරුෂ භාවය" : "GENDER"
        );

        dobValue = addInfo(
            isSinhala() ? "උපන් දිනය" : "DATE OF BIRTH"
        );

        positionValue = addInfo(
            isSinhala() ? "රැකියා තනතුර" : "JOB POSITION"
        );

        accountStatusValue = addInfo(
            isSinhala() ? "ගිණුමේ තත්ත්වය" : "ACCOUNT STATUS"
        );

        registrationDateValue = addInfo(
            isSinhala() ? "ලියාපදිංචි වූ දිනය" : "REGISTRATION DATE"
        );

        statusText = new TextView(this);
        statusText.setTextColor(GRAY);
        statusText.setTextSize(13);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(dp(8), dp(8), dp(8), dp(8));

        mainLayout.addView(
            statusText,
            new LinearLayout.LayoutParams(-1, -2)
        );

        refreshButton = new Button(this);
        refreshButton.setText(
            isSinhala()
                ? "තොරතුරු යාවත්කාලීන කරන්න"
                : "REFRESH PROFILE"
        );

        refreshButton.setTextColor(WHITE);
        refreshButton.setTextSize(14);
        refreshButton.setAllCaps(false);
        refreshButton.setBackground(round(RED, dp(12), RED));

        LinearLayout.LayoutParams refreshParams =
            new LinearLayout.LayoutParams(-1, dp(52));

        refreshParams.setMargins(0, dp(10), 0, dp(10));

        mainLayout.addView(refreshButton, refreshParams);

        refreshButton.setOnClickListener(v -> loadProfile());

        Button backButton = new Button(this);
        backButton.setText(
            isSinhala() ? "ආපසු යන්න" : "BACK"
        );
        backButton.setTextColor(RED);
        backButton.setTextSize(14);
        backButton.setAllCaps(false);
        backButton.setBackground(round(WHITE, dp(12), RED));

        mainLayout.addView(
            backButton,
            new LinearLayout.LayoutParams(-1, dp(48))
        );

        backButton.setOnClickListener(v -> finish());
    }


    private TextView addInfo(String label) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(9), dp(12), dp(9));
        item.setBackground(round(
            Color.rgb(248, 248, 248),
            dp(9),
            Color.rgb(235, 235, 235)
        ));

        LinearLayout.LayoutParams itemParams =
            new LinearLayout.LayoutParams(-1, -2);

        itemParams.setMargins(0, dp(5), 0, dp(5));
        profileCard.addView(item, itemParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(GRAY);
        labelView.setTextSize(11);
        labelView.setTypeface(null, Typeface.BOLD);

        item.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText("...");
        valueView.setTextColor(BLACK);
        valueView.setTextSize(15);
        valueView.setPadding(0, dp(5), 0, dp(2));

        item.addView(valueView);

        return valueView;
    }


    private void loadProfile() {

        if (sessionToken.isEmpty()) {
            showStatus(
                isSinhala()
                    ? "Login සැසිය හමු නොවීය."
                    : "Login session not found."
            );
            return;
        }

        refreshButton.setEnabled(false);
        refreshButton.setText(
            isSinhala() ? "තොරතුරු ලබාගනිමින්..." : "LOADING..."
        );

        showStatus(
            isSinhala()
                ? "සේවාදායකයෙන් තොරතුරු ලබාගනිමින්..."
                : "Loading profile from server..."
        );

        executor.execute(() -> {

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
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                );

                JSONObject request = new JSONObject();

                request.put("action", "getMyProfile");
                request.put("sessionToken", sessionToken);

                byte[] requestBytes =
                    request.toString().getBytes(StandardCharsets.UTF_8);

                try (OutputStream output =
                         connection.getOutputStream()) {
                    output.write(requestBytes);
                    output.flush();
                }

                int responseCode = connection.getResponseCode();

                InputStream inputStream =
                    (responseCode >= 200 && responseCode < 400)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                if (inputStream == null) {
                    throw new Exception("Empty server response.");
                }

                StringBuilder response = new StringBuilder();

                try (BufferedReader reader =
                         new BufferedReader(
                             new InputStreamReader(
                                 inputStream,
                                 StandardCharsets.UTF_8
                             )
                         )) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject result =
                    new JSONObject(response.toString());

                if (!result.optBoolean("success", false)) {

                    String message = result.optString(
                        "message",
                        "Unable to load profile."
                    );

                    throw new Exception(message);
                }

                JSONObject employee =
                    result.optJSONObject("employee");

                if (employee == null) {
                    throw new Exception("Employee data not found.");
                }

                JSONObject photo =
                    result.optJSONObject("profilePhoto");

                Bitmap photoBitmap = null;

                if (photo != null) {

                    String base64 = photo.optString("base64", "");

                    if (!base64.isEmpty()) {

                        byte[] imageBytes =
                            android.util.Base64.decode(
                                base64,
                                android.util.Base64.DEFAULT
                            );

                        photoBitmap = BitmapFactory.decodeByteArray(
                            imageBytes,
                            0,
                            imageBytes.length
                        );
                    }
                }

                JSONObject finalEmployee = employee;
                Bitmap finalPhotoBitmap = photoBitmap;

                mainHandler.post(() -> {

                    displayProfile(finalEmployee, finalPhotoBitmap);

                    showStatus(
                        isSinhala()
                            ? "තොරතුරු සාර්ථකව ලබාගත්තා."
                            : "Profile loaded successfully."
                    );

                    refreshButton.setEnabled(true);
                    refreshButton.setText(
                        isSinhala()
                            ? "තොරතුරු යාවත්කාලීන කරන්න"
                            : "REFRESH PROFILE"
                    );
                });

            } catch (Exception error) {

                String message = error.getMessage();

                mainHandler.post(() -> {

                    showStatus(
                        isSinhala()
                            ? "තොරතුරු ලබාගැනීමට නොහැකි විය: " + message
                            : "Unable to load profile: " + message
                    );

                    refreshButton.setEnabled(true);
                    refreshButton.setText(
                        isSinhala()
                            ? "නැවත උත්සාහ කරන්න"
                            : "TRY AGAIN"
                    );
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    private void displayProfile(
        JSONObject employee,
        Bitmap photoBitmap
    ) {

        employeeIDValue.setText(
            getValue(employee, "employeeID")
        );

        fullNameValue.setText(
            getValue(employee, "fullName")
        );

        mobileValue.setText(
            getValue(employee, "mobileNumber")
        );

        addressValue.setText(
            getValue(employee, "address")
        );

        genderValue.setText(
            getValue(employee, "gender")
        );

        dobValue.setText(
            getValue(employee, "dateOfBirth")
        );

        positionValue.setText(
            getValue(employee, "jobPosition")
        );

        accountStatusValue.setText(
            getValue(employee, "accountStatus")
        );

        registrationDateValue.setText(
            getValue(employee, "registrationDate")
        );

        if (photoBitmap != null) {
            profileImage.setImageBitmap(photoBitmap);
        } else {
            profileImage.setImageDrawable(
                round(RED, dp(60), RED)
            );
        }
    }


    private String getValue(JSONObject object, String key) {

        String value = object.optString(key, "");

        if (value == null || value.trim().isEmpty() ||
            "null".equalsIgnoreCase(value)) {

            return isSinhala()
                ? "ලබාගත නොහැක"
                : "Not Available";
        }

        return value;
    }


    private void showStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }


    private int dp(float value) {
        return (int) (
            value * getResources().getDisplayMetrics().density
        );
    }


    private GradientDrawable round(
        int color,
        int radius,
        int strokeColor
    ) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), strokeColor);

        return drawable;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
