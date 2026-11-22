package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonthlyReportsActivity extends Activity {

    private static final String API_URL =
        "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private final int WHITE = Color.WHITE;

    private String token;
    private boolean sinhala;
    private LinearLayout reportList;
    private TextView statusText;
    private byte[] pendingPdf;
    private String pendingFileName;

    private static final int CREATE_PDF_FILE = 5101;

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
                "නැවත Login වන්න." : "Please log in again.");
        } else {
            loadReports();
        }
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(22),dp(18),dp(25));
        scroll.addView(root);

        TextView title = text(
            sinhala ? "මාසික වාර්තා" : "MONTHLY REPORTS",
            24,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full());

        TextView subtitle = text(
            sinhala ? "ඔබගේ මාසික ක්‍රියාකාරකම් PDF වාර්තා" :
                "YOUR MONTHLY ACTIVITY PDF REPORTS",
            13,WHITE,false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(8),0,dp(18));
        root.addView(subtitle,full());

        Button refresh = button(
            sinhala ? "නැවත ලබාගන්න" : "REFRESH",
            GOLD,NAVY);
        root.addView(refresh,buttonParams());
        refresh.setOnClickListener(v -> loadReports());

        statusText = text("",14,GOLD,false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0,dp(14),0,dp(14));
        root.addView(statusText,full());

        reportList = new LinearLayout(this);
        reportList.setOrientation(LinearLayout.VERTICAL);
        root.addView(reportList,full());

        setContentView(scroll);
    }

    private TextView text(String value,int size,int color,boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null,Typeface.BOLD);
        return t;
    }

    private Button button(String value,int bg,int fg) {
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

    private void loadReports() {
        statusText.setText(sinhala ?
            "වාර්තා ලබාගනිමින්..." : "Loading reports...");

        reportList.removeAllViews();

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action","getMyMonthlyReports");
                request.put("sessionToken",token);

                JSONObject result = post(request);

                runOnUiThread(() -> showReports(result));

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText(
                    sinhala ?
                    "වාර්තා ලබාගැනීමට නොහැකි වුණා." :
                    "Could not load reports."));
            }
        });
    }

    private void showReports(JSONObject result) {
        reportList.removeAllViews();

        if (!result.optBoolean("success",false)) {
            statusText.setText(result.optString("message",
                sinhala ? "දත්ත ලබාගැනීම අසාර්ථකයි." :
                    "Request failed."));
            return;
        }

        JSONArray reports = result.optJSONArray("reports");

        if (reports == null || reports.length() == 0) {
            statusText.setText(sinhala ?
                "ඔබට තවම මාසික PDF වාර්තා ලබාදී නැහැ." :
                "No monthly PDF reports are available yet.");
            return;
        }

        statusText.setText(sinhala ?
            "ලබාදී ඇති වාර්තා: " + reports.length() :
            "Available reports: " + reports.length());

        for (int i=0; i<reports.length(); i++) {
            JSONObject report = reports.optJSONObject(i);
            if (report != null) addReportCard(report);
        }
    }

    private void addReportCard(JSONObject report) {
        String reportID = report.optString("reportID","");
        String month = report.optString("reportMonth","");
        String fileName = report.optString("fileName","Monthly_Report.pdf");
        String reportStatus = report.optString("reportStatus","");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15),dp(14),dp(15),dp(14));
        card.setBackgroundColor(Color.rgb(37,51,70));

        LinearLayout.LayoutParams cp = full();
        cp.bottomMargin = dp(13);
        reportList.addView(card,cp);

        TextView heading = text(
            month.isEmpty() ?
                (sinhala ? "මාසික වාර්තාව" : "Monthly Report") :
                month,
            19,GOLD,true);
        card.addView(heading,full());

        if (!reportStatus.isEmpty()) {
            TextView state = text(
                (sinhala ? "තත්ත්වය: " : "Status: ") + reportStatus,
                13,WHITE,false);
            state.setPadding(0,dp(7),0,dp(7));
            card.addView(state,full());
        }

        Button download = button(
            sinhala ? "PDF වාර්තාව ලබාගන්න" : "DOWNLOAD PDF",
            GOLD,NAVY);
        LinearLayout.LayoutParams bp = buttonParams();
        bp.topMargin = dp(8);
        card.addView(download,bp);

        download.setOnClickListener(v -> {
            if (reportID.isEmpty()) {
                statusText.setText(sinhala ?
                    "වාර්තා අංකය නොමැත." :
                    "Report ID is missing.");
                return;
            }
            downloadReport(reportID,fileName,download);
        });
    }

    private void downloadReport(
        String reportID,String fileName,Button downloadButton) {

        downloadButton.setEnabled(false);
        downloadButton.setText(sinhala ?
            "PDF ලබාගනිමින්..." : "DOWNLOADING...");

        statusText.setText(sinhala ?
            "PDF ගොනුව සකස් කරමින්..." :
            "Preparing PDF file...");

        executor.execute(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action","downloadMyMonthlyReport");
                request.put("sessionToken",token);
                request.put("reportID",reportID);

                JSONObject result = post(request);

                if (!result.optBoolean("success",false)) {
                    throw new Exception(result.optString("message",
                        "Download failed"));
                }

                String encoded = result.optString("pdfBase64","");
                if (encoded.isEmpty()) {
                    throw new Exception("PDF data is empty");
                }

                byte[] pdf = android.util.Base64.decode(
                    encoded,android.util.Base64.DEFAULT);

                if (pdf.length < 5 ||
                    pdf[0] != '%' || pdf[1] != 'P' ||
                    pdf[2] != 'D' || pdf[3] != 'F') {
                    throw new Exception("Invalid PDF file");
                }

                String returnedName =
                    result.optString("fileName",fileName);

                runOnUiThread(() -> {
                    pendingPdf = pdf;
                    pendingFileName = safeFileName(returnedName);

                    downloadButton.setEnabled(true);
                    downloadButton.setText(sinhala ?
                        "PDF වාර්තාව ලබාගන්න" : "DOWNLOAD PDF");

                    Intent intent =
                        new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_TITLE,
                        pendingFileName);

                    startActivityForResult(intent,CREATE_PDF_FILE);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    downloadButton.setEnabled(true);
                    downloadButton.setText(sinhala ?
                        "PDF වාර්තාව ලබාගන්න" : "DOWNLOAD PDF");
                    statusText.setText(sinhala ?
                        "PDF ලබාගැනීමට නොහැකි වුණා. නැවත උත්සාහ කරන්න." :
                        "Could not download PDF. Please try again.");
                });
            }
        });
    }

    private String safeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Monthly_Report.pdf";
        }
        String safe = name.replaceAll("[\\\\/:*?\"<>|]","_");
        if (!safe.toLowerCase().endsWith(".pdf")) safe += ".pdf";
        return safe;
    }

    @Override
    protected void onActivityResult(
        int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == CREATE_PDF_FILE &&
            resultCode == RESULT_OK && data != null &&
            data.getData() != null && pendingPdf != null) {

            Uri uri = data.getData();

            try (OutputStream out =
                getContentResolver().openOutputStream(uri)) {

                if (out == null) throw new IOException("Cannot open file");
                out.write(pendingPdf);
                out.flush();

                statusText.setText(sinhala ?
                    "PDF වාර්තාව සාර්ථකව සුරැකුවා." :
                    "PDF report saved successfully.");

                pendingPdf = null;

            } catch (Exception e) {
                statusText.setText(sinhala ?
                    "PDF ගොනුව සුරැකීමට නොහැකි වුණා." :
                    "Could not save PDF file.");
            }
        }
    }

    private JSONObject post(JSONObject data) throws Exception {
        HttpURLConnection connection =
            (HttpURLConnection)new URL(API_URL).openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(60000);
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
