package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import android.content.res.ColorStateList;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class DayEndActivity extends Activity {
    private static final String API_URL = "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private String token, today;
    private boolean sinhala, submitted = false;
    private TextView jobs, value, commission, earnings, status;
    private EditText summary;
    private Button submit, refresh;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences p = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE);
        token = p.getString("workerSessionToken", "");
        sinhala = "si".equalsIgnoreCase(p.getString("language", "si"));
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        buildUI();
        if (token.isEmpty()) {
            status.setText(sinhala ? "නැවත Login වන්න." : "Please log in again.");
            buttons(false);
        } else loadReport();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(dp(20),dp(22),dp(20),dp(25));
        scroll.addView(root);

        TextView title = text(sinhala ? "දවස අවසාන වාර්තාව" : "DAY END REPORT",23,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, lp());

        TextView date = text((sinhala ? "දිනය: " : "Date: ")+today,16,Color.WHITE,false);
        date.setGravity(Gravity.CENTER);
        date.setPadding(0,dp(12),0,dp(18));
        root.addView(date,lp());

        addHeading(root,sinhala ? "අද දවසේ සාරාංශය" : "TODAY'S SUMMARY");
        jobs = addRow(root,sinhala ? "මුළු වැඩ ගණන" : "Total Jobs","0");
        value = addRow(root,sinhala ? "මුළු වැඩ වටිනාකම" : "Total Job Value","Rs. 0.00");
        commission = addRow(root,sinhala ? "මුළු කොමිස් මුදල" : "Total Commission","Rs. 0.00");
        earnings = addRow(root,sinhala ? "මුළු ආදායම" : "Total Earnings","Rs. 0.00");

        addHeading(root,sinhala ? "අද කළ වැඩ පිළිබඳ සටහන" : "WORK SUMMARY");
        summary = new EditText(this);
        summary.setTextColor(Color.WHITE);
        summary.setHintTextColor(Color.LTGRAY);
        summary.setHint(sinhala ? "අද කළ වැඩ පිළිබඳ විස්තරයක් ලියන්න..." : "Describe the work you completed today...");
        summary.setGravity(Gravity.TOP|Gravity.START);
        summary.setMinLines(4);
        summary.setPadding(dp(12),dp(12),dp(12),dp(12));
        summary.setBackgroundTintList(ColorStateList.valueOf(GOLD));
        root.addView(summary,new LinearLayout.LayoutParams(-1,dp(120)));

        status = text("",14,GOLD,false);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0,dp(15),0,dp(10));
        root.addView(status,lp());

        submit = button(sinhala ? "වාර්තාව Submit කරන්න" : "SUBMIT DAY END REPORT",GOLD,NAVY);
        root.addView(submit,buttonLP());
        submit.setOnClickListener(v -> submitReport());

        refresh = button(sinhala ? "නැවත පරීක්ෂා කරන්න" : "REFRESH REPORT",Color.rgb(49,69,94),Color.WHITE);
        root.addView(refresh,buttonLP());
        refresh.setOnClickListener(v -> loadReport());
        setContentView(scroll);
    }

    private TextView text(String s,int size,int color,boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold) t.setTypeface(null,Typeface.BOLD);
        return t;
    }

    private void addHeading(LinearLayout root,String s) {
        TextView t=text(s,15,GOLD,true);
        t.setPadding(0,dp(16),0,dp(8));
        root.addView(t,lp());
    }

    private TextView addRow(LinearLayout root,String label,String initial) {
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),dp(12),dp(12),dp(12));
        row.setBackgroundColor(Color.rgb(37,51,70));
        LinearLayout.LayoutParams rp=lp(); rp.bottomMargin=dp(6);
        root.addView(row,rp);
        TextView left=text(label,14,Color.WHITE,false);
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        TextView right=text(initial,14,GOLD,true);
        row.addView(right);
        return right;
    }

    private Button button(String label,int bg,int fg) {
        Button b=new Button(this);
        b.setText(label); b.setTextColor(fg); b.setTextSize(14);
        b.setTypeface(null,Typeface.BOLD); b.setAllCaps(false);
        b.setBackgroundTintList(ColorStateList.valueOf(bg));
        return b;
    }

    private LinearLayout.LayoutParams lp() {
        return new LinearLayout.LayoutParams(-1,-2);
    }

    private LinearLayout.LayoutParams buttonLP() {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));
        p.topMargin=dp(9); return p;
    }

    private int dp(int n) {
        return (int)(n*getResources().getDisplayMetrics().density+0.5f);
    }

    private void loadReport() {
        buttons(false);
        status.setText(sinhala ? "වාර්තාව ලබාගනිමින්..." : "Loading report...");
        executor.execute(() -> {
            try {
                JSONObject q=new JSONObject();
                q.put("action","getMyDayEndReport");
                q.put("sessionToken",token);
                q.put("workDate",today);
                JSONObject r=post(q);
                runOnUiThread(() -> showReport(r));
            } catch(Exception e) {
                runOnUiThread(() -> {
                    status.setText(sinhala ? "දත්ත ලබාගැනීමට නොහැකි වුණා." : "Could not load report. Try again.");
                    buttons(true);
                });
            }
        });
    }

    private void showReport(JSONObject r) {
        if(!r.optBoolean("success",false)) {
            status.setText(r.optString("message","Request failed"));
            buttons(true); return;
        }
        JSONObject data=r.optJSONObject("report");
        if(data==null) {
            submitted=false;
            jobs.setText("0"); value.setText("Rs. 0.00");
            commission.setText("Rs. 0.00"); earnings.setText("Rs. 0.00");
            status.setText(sinhala ? "අද වාර්තාව තවම Submit කර නැහැ." : "No report submitted yet today.");
            buttons(true); return;
        }
        submitted=true;
        jobs.setText(""+data.optInt("totalJobs",0));
        value.setText("Rs. "+money(data.optDouble("totalJobValue",0)));
        commission.setText("Rs. "+money(data.optDouble("totalCommission",0)));
        earnings.setText("Rs. "+money(data.optDouble("totalEarnings",0)));
        summary.setText(data.optString("workSummary",""));
        status.setText((sinhala ? "වාර්තා තත්ත්වය: " : "Report status: ")+data.optString("reportStatus","PENDING"));
        submit.setEnabled(false);
        submit.setText(sinhala ? "වාර්තාව Submit කර ඇත" : "REPORT ALREADY SUBMITTED");
        refresh.setEnabled(true);
    }

    private void submitReport() {
        if(submitted) return;
        buttons(false);
        status.setText(sinhala ? "වාර්තාව Submit කරමින්..." : "Submitting report...");
        String note=summary.getText().toString().trim();
        executor.execute(() -> {
            try {
                JSONObject q=new JSONObject();
                q.put("action","submitDayEndReport");
                q.put("sessionToken",token);
                q.put("workSummary",note);
                JSONObject r=post(q);
                runOnUiThread(() -> {
                    if(r.optBoolean("success",false)) {
                        JSONObject data=r.optJSONObject("report");
                        if(data!=null) {
                            jobs.setText(""+data.optInt("totalJobs",0));
                            value.setText("Rs. "+money(data.optDouble("totalJobValue",0)));
                            commission.setText("Rs. "+money(data.optDouble("totalCommission",0)));
                            earnings.setText("Rs. "+money(data.optDouble("totalEarnings",0)));
                        }
                        submitted=true;
                        status.setText(r.optString("message",sinhala ? "වාර්තාව යැව්වා." : "Report submitted."));
                        submit.setEnabled(false);
                        submit.setText(sinhala ? "වාර්තාව Submit කර ඇත" : "REPORT ALREADY SUBMITTED");
                        refresh.setEnabled(true);
                    } else {
                        status.setText(r.optString("message",sinhala ? "වාර්තාව යැවීමට නොහැකි වුණා." : "Submission failed."));
                        buttons(true);
                    }
                });
            } catch(Exception e) {
                runOnUiThread(() -> {
                    status.setText(sinhala ? "සම්බන්ධතාවයේ ගැටලුවක්. නැවත උත්සාහ කරන්න." : "Connection error. Try again.");
                    buttons(true);
                });
            }
        });
    }

    private JSONObject post(JSONObject data) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(API_URL).openConnection();
        try {
            c.setRequestMethod("POST");
            c.setConnectTimeout(20000); c.setReadTimeout(30000);
            c.setDoOutput(true); c.setInstanceFollowRedirects(true);
            c.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            try(OutputStream out=c.getOutputStream()) {
                out.write(data.toString().getBytes("UTF-8"));
            }
            int code=c.getResponseCode();
            InputStream in=(code>=200 && code<400)?c.getInputStream():c.getErrorStream();
            if(in==null) throw new Exception("Empty response: "+code);
            StringBuilder result=new StringBuilder();
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(in,"UTF-8"))) {
                String line; while((line=reader.readLine())!=null) result.append(line);
            }
            return new JSONObject(result.toString());
        } finally { c.disconnect(); }
    }

    private String money(double n) {
        return String.format(Locale.US,"%.2f",n);
    }

    private void buttons(boolean enabled) {
        if(submit!=null) submit.setEnabled(enabled && !submitted);
        if(refresh!=null) refresh.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
