package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyWorkActivity extends Activity {

    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<JobType> jobTypes = new ArrayList<>();
    private final ArrayList<Uri> selectedPhotos = new ArrayList<>();

    private Spinner spinnerJobType;
    private EditText txtDescription, txtJobValue;
    private TextView lblCommission, lblPhotos, lblHistory;
    private Button btnSubmit;
    private String sessionToken = "";
    private double commissionRate = 0;

    private final int NAVY = Color.rgb(16, 27, 45);
    private final int GOLD = Color.rgb(214, 173, 85);
    private final int WHITE = Color.WHITE;

    private static class JobType {
        String id, name;
        double rate;
        JobType(String id, String name, double rate) {
            this.id = id; this.name = name; this.rate = rate;
        }
        @Override public String toString() { return name; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionToken = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE)
            .getString("workerSessionToken", "");

        buildUI();

        if (sessionToken.isEmpty()) {
            showMessage("Session expired. Please log in again.");
            btnSubmit.setEnabled(false);
        } else {
            loadJobTypes();
            loadTodayWork();
        }
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(24));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("DAILY WORK");
        title.setTextColor(GOLD);
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(8));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Submit your daily work details");
        subtitle.setTextColor(WHITE);
        subtitle.setTextSize(14);
        root.addView(subtitle);

        addLabel(root, "Job Type");
        spinnerJobType = new Spinner(this);
        spinnerJobType.setBackgroundColor(WHITE);
        root.addView(spinnerJobType, matchWrap());

        addLabel(root, "Work Description");
        txtDescription = editText("Enter work details", InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_FLAG_MULTI_LINE, 4);
        root.addView(txtDescription, matchWrap());

        addLabel(root, "Job Value (Rs.)");
        txtJobValue = editText("Enter job value", InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_FLAG_DECIMAL, 1);
        root.addView(txtJobValue, matchWrap());

        lblCommission = new TextView(this);
        lblCommission.setText("Estimated Commission: Rs. 0.00");
        lblCommission.setTextColor(GOLD);
        lblCommission.setTextSize(16);
        lblCommission.setTypeface(null, Typeface.BOLD);
        lblCommission.setPadding(0, dp(12), 0, dp(12));
        root.addView(lblCommission);

        txtJobValue.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) {
                updateCommission();
            }
            public void afterTextChanged(android.text.Editable e) {}
        });

        Button btnPhotos = makeButton("SELECT WORK PHOTOS (MAX 5)");
        root.addView(btnPhotos, matchWrap());
        btnPhotos.setOnClickListener(v -> choosePhotos());

        lblPhotos = new TextView(this);
        lblPhotos.setText("No photos selected");
        lblPhotos.setTextColor(WHITE);
        lblPhotos.setPadding(0, dp(8), 0, dp(12));
        root.addView(lblPhotos);

        btnSubmit = makeButton("SUBMIT DAILY WORK");
        root.addView(btnSubmit, matchWrap());
        btnSubmit.setOnClickListener(v -> submitWork());

        Button btnRefresh = makeButton("REFRESH TODAY'S WORK");
        root.addView(btnRefresh, matchWrap());
        btnRefresh.setOnClickListener(v -> loadTodayWork());

        addLabel(root, "TODAY'S SUBMITTED WORK");
        lblHistory = new TextView(this);
        lblHistory.setTextColor(WHITE);
        lblHistory.setTextSize(14);
        lblHistory.setPadding(0, dp(8), 0, dp(12));
        root.addView(lblHistory);

        setContentView(scroll);
    }

    private void addLabel(LinearLayout root, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(GOLD);
        label.setTextSize(15);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, dp(18), 0, dp(7));
        root.addView(label);
    }

    private EditText editText(String hint, int inputType, int lines) {
        EditText edit = new EditText(this);
        edit.setHint(hint);
        edit.setTextColor(Color.BLACK);
        edit.setHintTextColor(Color.DKGRAY);
        edit.setTextSize(16);
        edit.setInputType(inputType);
        edit.setSingleLine(lines == 1);
        if (lines > 1) {
            edit.setMinLines(lines);
            edit.setGravity(android.view.Gravity.TOP);
        }
        edit.setPadding(dp(12), dp(10), dp(12), dp(10));
        edit.setBackgroundColor(WHITE);
        return edit;
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(NAVY);
        b.setTextSize(14);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GOLD));
        return b;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(5), 0, dp(8));
        return p;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showMessage(String message) {
        new android.app.AlertDialog.Builder(this)
            .setMessage(message).setPositiveButton("OK", null).show();
    }

    private JSONObject post(JSONObject payload) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream out = connection.getOutputStream()) {
            out.write(payload.toString().getBytes("UTF-8"));
        }

        int code = connection.getResponseCode();
        InputStream input = code >= 200 && code < 400
            ? connection.getInputStream() : connection.getErrorStream();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int n;
        while ((n = input.read(data)) != -1) buffer.write(data, 0, n);
        input.close();
        connection.disconnect();

        return new JSONObject(buffer.toString("UTF-8"));
    }

    private JSONObject request(String action) throws Exception {
        JSONObject data = new JSONObject();
        data.put("action", action);
        data.put("sessionToken", sessionToken);
        return post(data);
    }

    private void loadJobTypes() {
        executor.execute(() -> {
            try {
                JSONObject result = request("getMyJobTypes");
                if (!result.optBoolean("success")) {
                    runOnUiThread(() -> showMessage(result.optString("message",
                        "Could not load job types.")));
                    return;
                }

                JSONArray array = result.optJSONArray("jobTypes");
                jobTypes.clear();
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        jobTypes.add(new JobType(
                            item.optString("jobTypeID"),
                            item.optString("jobTypeName"),
                            item.optDouble("commissionRate", 0)
                        ));
                    }
                }

                runOnUiThread(() -> {
                    ArrayAdapter<JobType> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, jobTypes);
                    adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                    spinnerJobType.setAdapter(adapter);
                    spinnerJobType.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> p, View v,
                                                       int position, long id) {
                                commissionRate = jobTypes.get(position).rate;
                                updateCommission();
                            }
                            public void onNothingSelected(AdapterView<?> p) {
                                commissionRate = 0;
                                updateCommission();
                            }
                        });
                    if (jobTypes.isEmpty()) {
                        showMessage("No active Job Types are configured for your position. Please contact Admin.");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showMessage("Job Types could not be loaded. Check your internet connection."));
            }
        });
    }

    private void updateCommission() {
        double value = 0;
        try { value = Double.parseDouble(txtJobValue.getText().toString().trim()); }
        catch (Exception ignored) {}
        double commission = value * commissionRate / 100.0;
        lblCommission.setText(String.format(Locale.US,
            "Estimated Commission: Rs. %.2f (%.2f%%)", commission, commissionRate));
    }

    private void choosePhotos() {
        if (selectedPhotos.size() >= 5) {
            showMessage("You can select up to five photos.");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 410);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 410 || resultCode != RESULT_OK || data == null) return;

        selectedPhotos.clear();
        if (data.getClipData() != null) {
            int count = Math.min(data.getClipData().getItemCount(), 5);
            for (int i = 0; i < count; i++) {
                selectedPhotos.add(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            selectedPhotos.add(data.getData());
        }

        lblPhotos.setText(selectedPhotos.size() + " photo(s) selected");
    }

    private String getFileName(Uri uri) {
        String name = "WorkPhoto.jpg";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) name = cursor.getString(index);
            }
        } catch (Exception ignored) {}
        return name;
    }

    private JSONObject photoPayload(Uri uri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }

        int sample = 1;
        while (bounds.outWidth / sample > 1200 ||
               bounds.outHeight / sample > 1200) sample *= 2;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        if (bitmap == null) throw new Exception("Could not read selected photo.");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int quality = 78;
        do {
            output.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output);
            quality -= 8;
        } while (output.size() > 650 * 1024 && quality >= 38);

        bitmap.recycle();

        JSONObject photo = new JSONObject();
        photo.put("mimeType", "image/jpeg");
        photo.put("base64", android.util.Base64.encodeToString(
            output.toByteArray(), android.util.Base64.NO_WRAP));
        photo.put("fileName", getFileName(uri));
        return photo;
    }

    private void submitWork() {
        if (jobTypes.isEmpty() || spinnerJobType.getSelectedItemPosition() < 0) {
            showMessage("Please select an available Job Type.");
            return;
        }

        String description = txtDescription.getText().toString().trim();
        String valueText = txtJobValue.getText().toString().trim();

        if (description.isEmpty()) {
            txtDescription.setError("Enter work description");
            return;
        }
        if (valueText.isEmpty()) {
            txtJobValue.setError("Enter job value");
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueText);
            if (value < 0 || value > 100000000) throw new Exception();
        } catch (Exception e) {
            txtJobValue.setError("Enter a valid job value");
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("SUBMITTING...");

        executor.execute(() -> {
            try {
                JobType selected = jobTypes.get(spinnerJobType.getSelectedItemPosition());

                JSONObject payload = new JSONObject();
                payload.put("action", "submitDailyWork");
                payload.put("sessionToken", sessionToken);
                payload.put("jobTypeID", selected.id);
                payload.put("description", description);
                payload.put("jobValue", value);

                JSONArray photos = new JSONArray();
                for (Uri uri : selectedPhotos) photos.put(photoPayload(uri));
                payload.put("photos", photos);

                JSONObject result = post(payload);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("SUBMIT DAILY WORK");
                    if (result.optBoolean("success")) {
                        JSONObject work = result.optJSONObject("work");
                        String message = result.optString("message",
                            "Daily work submitted successfully.");
                        if (work != null) {
                            message += "\n\nCommission: Rs. " +
                                work.optString("commissionAmount", "0") +
                                "\nStatus: " + work.optString("reviewStatus", "PENDING");
                        }
                        showMessage(message);
                        txtDescription.setText("");
                        txtJobValue.setText("");
                        selectedPhotos.clear();
                        lblPhotos.setText("No photos selected");
                        updateCommission();
                        loadTodayWork();
                    } else {
                        showMessage(result.optString("message",
                            "Work submission failed."));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("SUBMIT DAILY WORK");
                    showMessage("Submission failed. Please check your internet connection and try again.");
                });
            }
        });
    }

    private void loadTodayWork() {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("action", "getMyDailyWork");
                payload.put("sessionToken", sessionToken);
                payload.put("workDate", new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.US).format(new Date()));

                JSONObject result = post(payload);
                JSONArray work = result.optJSONArray("work");

                StringBuilder text = new StringBuilder();
                if (result.optBoolean("success") && work != null && work.length() > 0) {
                    for (int i = 0; i < work.length(); i++) {
                        JSONObject item = work.getJSONObject(i);
                        text.append("\n")
                            .append(i + 1).append(". ")
                            .append(item.optString("jobType")).append("\n")
                            .append("Job Value: Rs. ")
                            .append(item.optString("jobValue")).append("\n")
                            .append("Commission: Rs. ")
                            .append(item.optString("commissionAmount")).append("\n")
                            .append("Approval: ")
                            .append(item.optString("reviewStatus")).append("\n")
                            .append("--------------------------------\n");
                    }
                } else {
                    text.append("No work records found for today.");
                }

                String finalText = text.toString();
                runOnUiThread(() -> lblHistory.setText(finalText));
            } catch (Exception e) {
                runOnUiThread(() ->
                    lblHistory.setText("Could not load today's work records."));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
