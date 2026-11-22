package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySalaryActivity extends Activity {

    private static final String API_URL =
            "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private final int WHITE = Color.WHITE;

    private String token;
    private boolean sinhala;

    private EditText monthInput;
    private TextView statusText;
    private TextView basicValue, commissionValue, allowanceValue;
    private TextView deductionValue, netValue, paymentStatusValue;
    private TextView paymentDateValue, paymentReferenceValue;
    private Button loadButton;

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
            loadButton.setEnabled(false);
        } else {
            loadSalary(getCurrentMonth());
        }
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(22),dp(18),dp(28));
        scroll.addView(root);

        TextView title = makeText(
                sinhala ? "මගේ වැටුප් විස්තර" : "MY SALARY",
                24,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full());

        TextView subtitle = makeText(
                sinhala ? "මාසික වැටුප් වාර්තාව" :
                        "MONTHLY SALARY STATEMENT",
                13,WHITE,false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(7),0,dp(22));
        root.addView(subtitle,full());

        addHeading(root,
                sinhala ? "මාසය තෝරන්න" : "SELECT MONTH");

        monthInput = new EditText(this);
        monthInput.setSingleLine(true);
        monthInput.setTextColor(WHITE);
        monthInput.setHintTextColor(Color.LTGRAY);
        monthInput.setHint("YYYY-MM");
        monthInput.setTextSize(17);
        monthInput.setGravity(Gravity.CENTER);
        monthInput.setText(getCurrentMonth());
        monthInput.setBackgroundTintList(
                ColorStateList.valueOf(GOLD));

        root.addView(monthInput,full());

        loadButton = makeButton(
                sinhala ? "වැටුප් විස්තර ලබාගන්න" :
                        "LOAD SALARY DETAILS",
                GOLD,NAVY);

        LinearLayout.LayoutParams bp = buttonParams();
        bp.topMargin = dp(14);
        root.addView(loadButton,bp);

        loadButton.setOnClickListener(v -> {
            String month = monthInput.getText().toString().trim();

            if (!month.matches("\\d{4}-(0[1-9]|1[0-2])")) {
                monthInput.setError(
                        sinhala ? "YYYY-MM ලෙස ඇතුළත් කරන්න." :
                                "Enter month as YYYY-MM.");
                return;
            }

            loadSalary(month);
        });

        statusText = makeText("",14,GOLD,false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0,dp(14),0,dp(12));
        root.addView(statusText,full());

        LinearLayout salaryCard = new LinearLayout(this);
        salaryCard.setOrientation(LinearLayout.VERTICAL);
        salaryCard.setPadding(dp(16),dp(16),dp(16),dp(16));

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.rgb(37,51,70));
        cardBackground.setCornerRadius(dp(15));
        salaryCard.setBackground(cardBackground);

        LinearLayout.LayoutParams cp = full();
        cp.topMargin = dp(8);
        root.addView(salaryCard,cp);

        addHeading(salaryCard,
                sinhala ? "වැටුප් විස්තර" : "SALARY BREAKDOWN");

        basicValue = addSalaryRow(salaryCard,
                sinhala ? "මූලික වැටුප" : "Basic Salary");

        commissionValue = addSalaryRow(salaryCard,
                sinhala ? "අනුමත කොමිස් මුදල" :
                        "Approved Commission");

        allowanceValue = addSalaryRow(salaryCard,
                sinhala ? "අමතර දීමනා" : "Allowances");

        deductionValue = addSalaryRow(salaryCard,
                sinhala ? "අඩු කිරීම්" : "Deductions");

        View divider = new View(this);
        divider.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams dpLine =
                new LinearLayout.LayoutParams(-1,dp(1));
        dpLine.topMargin = dp(12);
        dpLine.bottomMargin = dp(12);
        salaryCard.addView(divider,dpLine);

        netValue = addSalaryRow(salaryCard,
                sinhala ? "ශුද්ධ වැටුප" : "NET SALARY");
        netValue.setTextColor(GOLD);
        netValue.setTextSize(23);
        netValue.setTypeface(null,Typeface.BOLD);

        addHeading(root,
                sinhala ? "ගෙවීම් තොරතුරු" : "PAYMENT DETAILS");

        LinearLayout paymentCard = new LinearLayout(this);
        paymentCard.setOrientation(LinearLayout.VERTICAL);
        paymentCard.setPadding(dp(16),dp(14),dp(16),dp(14));
        paymentCard.setBackground(cardBackground);
        root.addView(paymentCard,full());

        paymentStatusValue = addSalaryRow(paymentCard,
                sinhala ? "ගෙවීම් තත්ත්වය" : "Payment Status");

        paymentDateValue = addSalaryRow(paymentCard,
                sinhala ? "ගෙවූ දිනය" : "Payment Date");

        paymentReferenceValue = addSalaryRow(paymentCard,
                sinhala ? "ගෙවීම් යොමු අංකය" : "Payment Reference");

        TextView note = makeText(
                sinhala ?
                        "සටහන: වැටුප් විස්තර Admin විසින් යාවත්කාලීන කරනු ලැබේ." :
                        "Note: Salary details are updated by the Admin.",
                12,Color.LTGRAY,false);

        note.setGravity(Gravity.CENTER);
        note.setPadding(0,dp(20),0,0);
        root.addView(note,full());

        setContentView(scroll);
        showNoSalary();
    }

    private TextView makeText(String value,int size,int color,
                              boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(null,Typeface.BOLD);
        }

        return t;
    }

    private void addHeading(LinearLayout root,String value) {
        TextView t = makeText(value,15,GOLD,true);
        t.setPadding(0,dp(15),0,dp(9));
        root.addView(t,full());
    }

    private TextView addSalaryRow(LinearLayout parent,String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(9),0,dp(9));

        TextView left = makeText(label,14,WHITE,false);
        TextView right = makeText("Rs. 0.00",15,WHITE,true);
        right.setGravity(Gravity.END);

        row.addView(left,new LinearLayout.LayoutParams(
                0,-2,1));

        row.addView(right,new LinearLayout.LayoutParams(
                0,-2,1));

        parent.addView(row,full());

        return right;
    }

    private Button makeButton(String value,int bg,int fg) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextSize(14);
        b.setTypeface(null,Typeface.BOLD);
        b.setTextColor(fg);
        b.setAllCaps(false);
        b.setBackgroundTintList(ColorStateList.valueOf(bg));
        return b;
    }

    private LinearLayout.LayoutParams full() {
        return new LinearLayout.LayoutParams(-1,-2);
    }

    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(-1,dp(54));
    }

    private int dp(int value) {
        return (int)(value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private String getCurrentMonth() {
        return new SimpleDateFormat(
                "yyyy-MM",Locale.US).format(new Date());
    }

    private void showNoSalary() {
        basicValue.setText("Rs. 0.00");
        commissionValue.setText("Rs. 0.00");
        allowanceValue.setText("Rs. 0.00");
        deductionValue.setText("Rs. 0.00");
        netValue.setText("Rs. 0.00");

        paymentStatusValue.setText(
                sinhala ? "තවම සකස් කර නැත" : "NOT CALCULATED");

        paymentDateValue.setText("-");
        paymentReferenceValue.setText("-");
    }

    private void loadSalary(String month) {
        loadButton.setEnabled(false);
        statusText.setText(
                sinhala ? "වැටුප් විස්තර ලබාගනිමින්..." :
                        "Loading salary details...");

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action","getMySalary");
                request.put("sessionToken",token);
                request.put("month",month);

                JSONObject result = post(request);

                runOnUiThread(() -> {
                    loadButton.setEnabled(true);

                    if (!result.optBoolean("success",false)) {
                        statusText.setText(result.optString(
                                "message",
                                sinhala ? "දත්ත ලබාගැනීම අසාර්ථකයි." :
                                        "Could not load salary."));
                        return;
                    }

                    JSONObject salary =
                            result.optJSONObject("salary");

                    if (salary == null ||
                            result.isNull("salary")) {
                        showNoSalary();
                        statusText.setText(
                                sinhala ?
                                        "මෙම මාසයට වැටුප් වාර්තාවක් තවම සකස් කර නැහැ." :
                                        "No salary record found for this month.");
                        return;
                    }

                    displaySalary(salary);
                    statusText.setText(
                            sinhala ?
                                    "වැටුප් විස්තර සාර්ථකව ලබාගත්තා." :
                                    "Salary details loaded successfully.");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadButton.setEnabled(true);
                    statusText.setText(
                            sinhala ?
                                    "සම්බන්ධතාවයේ දෝෂයක්. නැවත උත්සාහ කරන්න." :
                                    "Connection error. Please try again.");
                });
            }
        });
    }

    private void displaySalary(JSONObject salary) {
        basicValue.setText(money(salary.optDouble("basicSalary",0)));
        commissionValue.setText(
                money(salary.optDouble("monthlyCommission",0)));
        allowanceValue.setText(
                money(salary.optDouble("allowances",0)));
        deductionValue.setText(
                money(salary.optDouble("deductions",0)));
        netValue.setText(
                money(salary.optDouble("netSalary",0)));

        String paymentStatus =
                salary.optString("paymentStatus","PENDING");

        paymentStatusValue.setText(paymentStatus);
        paymentDateValue.setText(
                emptyAsDash(salary.optString("paymentDate","")));
        paymentReferenceValue.setText(
                emptyAsDash(salary.optString("paymentReference","")));
    }

    private String money(double amount) {
        return String.format(Locale.US,"Rs. %,.2f",amount);
    }

    private String emptyAsDash(String value) {
        return value == null || value.trim().isEmpty()
                ? "-" : value;
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
