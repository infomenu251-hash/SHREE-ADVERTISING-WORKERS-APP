
package com.shreeadvertising.workers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationActivity extends Activity {

    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final int RED = Color.rgb(215,25,32);
    private final int YELLOW = Color.rgb(255,212,0);
    private final int BLACK = Color.rgb(25,25,25);

    private EditText fullName, mobile, address, dob, password;
    private Spinner gender, position;
    private Button profileButton, nicFrontButton, nicBackButton, submitButton;
    private TextView heading;

    private String language = "si";
    private Uri profileUri, frontUri, backUri;
    private int imageChoice = 0;

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    private final String[] positions = {
        "Graphic Designers",
        "Printing Machine Operators",
        "Banner Installers",
        "Sales & Marketing",
        "Office Staff",
        "Drivers",
        "Sales Executives",
        "Sales Agents",
        "Web Developers",
        "Android Developers",
        "Software Engineers",
        "Work From Home"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SharedPreferences prefs =
                getSharedPreferences("WorkerAppSettings", MODE_PRIVATE);

            language = prefs.getString("language", "si");

            getWindow().setStatusBarColor(RED);
            getWindow().setNavigationBarColor(BLACK);

            buildScreen();

        } catch (Throwable e) {
            showStartupError(e);
        }
    }

    private void showStartupError(Throwable error) {
        String details = android.util.Log.getStackTraceString(error);

        android.util.Log.e("RegistrationActivity",
            "Registration screen failed to open", error);

        TextView errorText = new TextView(this);
        errorText.setText(details);
        errorText.setTextColor(Color.BLACK);
        errorText.setTextSize(13);
        errorText.setPadding(dp(16), dp(16), dp(16), dp(16));

        ScrollView errorScroll = new ScrollView(this);
        errorScroll.addView(errorText);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Registration Screen Error")
            .setView(errorScroll)
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void buildScreen() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(25), dp(22), dp(30));
        root.setBackgroundColor(Color.WHITE);

        scroll.addView(root);
        setContentView(scroll);

        heading = text(language.equals("si")
            ? "සේවක ලියාපදිංචිය"
            : "WORKER REGISTRATION", 25, RED, true);

        heading.setGravity(Gravity.CENTER);
        root.addView(heading, params());

        TextView note = text(language.equals("si")
            ? "ඔබගේ නිවැරදි තොරතුරු ඇතුළත් කරන්න"
            : "Please enter your correct details", 14, Color.GRAY, false);

        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(5), 0, dp(22));
        root.addView(note, params());

        fullName = addEdit(root,
            language.equals("si") ? "සම්පූර්ණ නම" : "Full Name");

        mobile = addEdit(root,
            language.equals("si") ? "දුරකථන අංකය" : "Mobile Number");

        mobile.setInputType(3);

        address = addEdit(root,
            language.equals("si") ? "ලිපිනය" : "Address");

        addLabel(root,
            language.equals("si") ? "ස්ත්‍රී පුරුෂ භාවය" : "Gender");

        gender = new Spinner(this);

        String[] genders = language.equals("si")
            ? new String[]{"තෝරන්න", "පිරිමි", "ගැහැණු", "වෙනත්"}
            : new String[]{"Select", "Male", "Female", "Other"};

        setSpinner(gender, genders);
        root.addView(gender, params());

        addLabel(root,
            language.equals("si") ? "උපන් දිනය" : "Date of Birth");

        dob = addEdit(root, language.equals("si")
            ? "උපන් දිනය තෝරන්න" : "Select Date of Birth");

        dob.setFocusable(false);
        dob.setOnClickListener(v -> chooseDate());

        addLabel(root,
            language.equals("si") ? "රැකියා තනතුර" : "Job Position");

        position = new Spinner(this);

        String[] jobItems = new String[positions.length + 1];

        jobItems[0] = language.equals("si") ? "තෝරන්න" : "Select";

        System.arraycopy(positions, 0, jobItems, 1, positions.length);

        setSpinner(position, jobItems);
        root.addView(position, params());

        password = addEdit(root,
            language.equals("si")
                ? "මුරපදය (අවම අක්ෂර 12)"
                : "Password (minimum 12 characters)");

        password.setInputType(129);

        addLabel(root,
            language.equals("si") ? "පැතිකඩ ඡායාරූපය" : "Profile Photo");

        profileButton = imageButton(language.equals("si")
            ? "ඡායාරූපය තෝරන්න" : "Choose Profile Photo");

        root.addView(profileButton, buttonParams());
        profileButton.setOnClickListener(v -> pickImage(1));

        addLabel(root,
            language.equals("si")
                ? "NIC ඉදිරිපස ඡායාරූපය"
                : "NIC Front Image");

        nicFrontButton = imageButton(language.equals("si")
            ? "ඉදිරිපස ඡායාරූපය තෝරන්න"
            : "Choose NIC Front");

        root.addView(nicFrontButton, buttonParams());
        nicFrontButton.setOnClickListener(v -> pickImage(2));

        addLabel(root,
            language.equals("si")
                ? "NIC පිටුපස ඡායාරූපය"
                : "NIC Back Image");

        nicBackButton = imageButton(language.equals("si")
            ? "පිටුපස ඡායාරූපය තෝරන්න"
            : "Choose NIC Back");

        root.addView(nicBackButton, buttonParams());
        nicBackButton.setOnClickListener(v -> pickImage(3));

        submitButton = new Button(this);

        submitButton.setText(language.equals("si")
            ? "ලියාපදිංචි වන්න" : "REGISTER");

        submitButton.setTextColor(Color.WHITE);
        submitButton.setTextSize(17);
        submitButton.setTypeface(null, Typeface.BOLD);
        submitButton.setBackgroundColor(RED);

        LinearLayout.LayoutParams bp = buttonParams();
        bp.setMargins(0, dp(25), 0, dp(15));

        root.addView(submitButton, bp);
        submitButton.setOnClickListener(v -> submitRegistration());

        Button back = new Button(this);

        back.setText(language.equals("si") ? "ආපසු" : "BACK");

        root.addView(back, buttonParams());
        back.setOnClickListener(v -> finish());
    }

    private EditText addEdit(LinearLayout root, String hint) {

        EditText e = new EditText(this);

        e.setSingleLine(true);
        e.setTextSize(16);
        e.setHint(hint);
        e.setPadding(dp(12), 0, dp(12), 0);
        e.setBackgroundColor(Color.rgb(245, 245, 245));

        LinearLayout.LayoutParams p = params();
        p.height = dp(52);
        p.setMargins(0, dp(4), 0, dp(13));

        root.addView(e, p);

        return e;
    }

    private void addLabel(LinearLayout root, String value) {

        TextView t = text(value, 14, BLACK, true);

        t.setPadding(0, dp(8), 0, dp(3));

        root.addView(t, params());
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

    private Button imageButton(String value) {

        Button b = new Button(this);

        b.setText(value);
        b.setTextColor(BLACK);
        b.setAllCaps(false);
        b.setBackgroundColor(YELLOW);

        return b;
    }

    private void setSpinner(Spinner spinner, String[] items) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            items
        );

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private LinearLayout.LayoutParams params() {

        return new LinearLayout.LayoutParams(
            -1,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams buttonParams() {

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, dp(52));

        p.setMargins(0, dp(5), 0, dp(8));

        return p;
    }

    private int dp(int value) {

        return (int) (
            value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    private void chooseDate() {

        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {

            dob.setText(String.format(
                java.util.Locale.US,
                "%04d-%02d-%02d",
                year,
                month + 1,
                day
            ));

        },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickImage(int choice) {

        imageChoice = choice;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            Uri selected = data.getData();

            if (selected == null) return;

            if (imageChoice == 1) {

                profileUri = selected;

                profileButton.setText(language.equals("si")
                    ? "පැතිකඩ ඡායාරූපය තෝරා ඇත"
                    : "Profile Photo Selected");

            } else if (imageChoice == 2) {

                frontUri = selected;

                nicFrontButton.setText(language.equals("si")
                    ? "NIC ඉදිරිපස තෝරා ඇත"
                    : "NIC Front Selected");

            } else if (imageChoice == 3) {

                backUri = selected;

                nicBackButton.setText(language.equals("si")
                    ? "NIC පිටුපස තෝරා ඇත"
                    : "NIC Back Selected");
            }
        }
    }

    private JSONObject imageData(Uri uri, String fileName)
        throws Exception {

        Bitmap bitmap;

        try (InputStream input =
                 getContentResolver().openInputStream(uri)) {

            bitmap = BitmapFactory.decodeStream(input);
        }

        if (bitmap == null) {
            throw new Exception("Invalid image");
        }

        int max = 1200;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min(
            1f,
            (float) max / Math.max(width, height)
        );

        if (scale < 1f) {

            Bitmap resized = Bitmap.createScaledBitmap(
                bitmap,
                Math.round(width * scale),
                Math.round(height * scale),
                true
            );

            bitmap.recycle();
            bitmap = resized;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int quality = 75;

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            quality,
            output
        );

        while (output.size() > 650 * 1024 && quality > 35) {

            output.reset();
            quality -= 10;

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                output
            );
        }

        bitmap.recycle();

        JSONObject image = new JSONObject();

        image.put("mimeType", "image/jpeg");

        image.put(
            "base64",
            Base64.encodeToString(
                output.toByteArray(),
                Base64.NO_WRAP
            )
        );

        image.put("fileName", fileName + ".jpg");

        return image;
    }

    private void submitRegistration() {

        String name = fullName.getText().toString().trim();
        String phone = mobile.getText().toString().trim();
        String home = address.getText().toString().trim();
        String birth = dob.getText().toString().trim();
        String pass = password.getText().toString();

        if (
            name.isEmpty() ||
            phone.isEmpty() ||
            home.isEmpty() ||
            birth.isEmpty() ||
            pass.isEmpty() ||
            gender.getSelectedItemPosition() == 0 ||
            position.getSelectedItemPosition() == 0 ||
            profileUri == null ||
            frontUri == null ||
            backUri == null
        ) {

            message(language.equals("si")
                ? "සියලු තොරතුරු සහ ඡායාරූප ඇතුළත් කරන්න."
                : "Please complete all fields and select all images.");

            return;
        }

        if (pass.length() < 12) {

            message(language.equals("si")
                ? "මුරපදය අවම වශයෙන් අක්ෂර 12ක් විය යුතුයි."
                : "Password must contain at least 12 characters.");

            return;
        }

        submitButton.setEnabled(false);

        submitButton.setText(language.equals("si")
            ? "යවමින්..." : "SUBMITTING...");

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject request = new JSONObject();

                request.put("action", "workerRegister");
                request.put("fullName", name);
                request.put("mobile", phone);
                request.put("address", home);

                request.put(
                    "gender",
                    new String[]{"", "Male", "Female", "Other"}[
                        gender.getSelectedItemPosition()
                    ]
                );

                request.put("dateOfBirth", birth);

                request.put(
                    "jobPosition",
                    positions[position.getSelectedItemPosition() - 1]
                );

                request.put("password", pass);

                request.put(
                    "profilePhoto",
                    imageData(profileUri, "profile")
                );

                request.put(
                    "nicFront",
                    imageData(frontUri, "nic_front")
                );

                request.put(
                    "nicBack",
                    imageData(backUri, "nic_back")
                );

                URL url = new URL(API_URL);

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(true);

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                try (OutputStream out = connection.getOutputStream()) {

                    out.write(
                        request.toString().getBytes(StandardCharsets.UTF_8)
                    );
                }

                InputStream stream = connection.getInputStream();

                ByteArrayOutputStream response =
                    new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];

                int count;

                while ((count = stream.read(buffer)) != -1) {
                    response.write(buffer, 0, count);
                }

                stream.close();

                JSONObject result = new JSONObject(
                    response.toString(StandardCharsets.UTF_8.name())
                );

                runOnUiThread(() -> {

                    if (result.optBoolean("success", false)) {

                        message(language.equals("si")
                            ? "ලියාපදිංචිය සාර්ථකයි. Admin අනුමැතිය ලැබෙන තුරු රැඳී සිටින්න."
                            : "Registration successful. Please wait for Admin approval.");

                        finish();

                    } else {

                        message(result.optString(
                            "message",
                            language.equals("si")
                                ? "ලියාපදිංචිය අසාර්ථකයි."
                                : "Registration failed."
                        ));

                        resetButton();
                    }
                });

            } catch (Exception e) {

                android.util.Log.e(
                    "RegistrationActivity",
                    "Registration submission failed",
                    e
                );

                runOnUiThread(() -> {

                    message(language.equals("si")
                        ? "දෝෂයක් ඇති වුණා. අන්තර්ජාල සම්බන්ධතාවය පරීක්ෂා කරන්න."
                        : "An error occurred. Please check your internet connection.");

                    resetButton();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void resetButton() {

        submitButton.setEnabled(true);

        submitButton.setText(language.equals("si")
            ? "ලියාපදිංචි වන්න" : "REGISTER");
    }

    private void message(String value) {

        Toast.makeText(
            this,
            value,
            Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}
