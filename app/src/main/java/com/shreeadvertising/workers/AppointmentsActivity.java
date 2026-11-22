package com.shreeadvertising.workers;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class AppointmentsActivity extends Activity {

    private static final String API_URL = "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private final int WHITE = Color.WHITE;

    private String token;
    private boolean sinhala;
    private EditText customer, phone, task, location, notes;
    private TextView dateText, timeText, statusText;
    private LinearLayout appointmentList;
    private Button saveButton;

    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("WorkerAppSettings", MODE_PRIVATE);
        token = prefs.getString("workerSessionToken", "");
        sinhala = "si".equalsIgnoreCase(prefs.getString("language", "si"));

        buildUI();

        if (token.isEmpty()) {
            statusText.setText(sinhala ? "නැවත Login වන්න." : "Please log in again.");
            saveButton.setEnabled(false);
        } else {
            loadAppointments();
        }
    }

    private void buildUI() {

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(20),dp(18),dp(25));
        scroll.addView(root);

        TextView title = makeText(
                sinhala ? "හමුවීම් කළමනාකරණය" : "MY APPOINTMENTS",
                23,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, full());

        addHeading(root,sinhala ? "නව හමුවීමක් එක් කරන්න" : "CREATE NEW APPOINTMENT");

        customer = addInput(root,
                sinhala ? "පාරිභෝගික නම" : "Customer Name");

        phone = addInput(root,
                sinhala ? "දුරකථන අංකය" : "Phone Number");

        task = addInput(root,
                sinhala ? "කළ යුතු කාර්යය" : "Task / Appointment Purpose");

        addHeading(root,sinhala ? "දිනය තෝරන්න" : "SELECT DATE");

        dateText = makeText(
                sinhala ? "දිනය තෝරා නැත" : "No date selected",
                15,WHITE,false);
        dateText.setGravity(Gravity.CENTER);
        dateText.setPadding(0,dp(10),0,dp(10));
        root.addView(dateText,full());

        Button dateButton = makeButton(
                sinhala ? "දිනය තෝරන්න" : "CHOOSE DATE", GOLD,NAVY);
        root.addView(dateButton,buttonParams());
        dateButton.setOnClickListener(v -> chooseDate());

        addHeading(root,sinhala ? "වේලාව තෝරන්න" : "SELECT TIME");

        timeText = makeText(
                sinhala ? "වේලාව තෝරා නැත" : "No time selected",
                15,WHITE,false);
        timeText.setGravity(Gravity.CENTER);
        timeText.setPadding(0,dp(10),0,dp(10));
        root.addView(timeText,full());

        Button timeButton = makeButton(
                sinhala ? "වේලාව තෝරන්න" : "CHOOSE TIME", GOLD,NAVY);
        root.addView(timeButton,buttonParams());
        timeButton.setOnClickListener(v -> chooseTime());

        location = addInput(root,
                sinhala ? "ස්ථානය (අත්‍යවශ්‍ය නොවේ)" : "Location (Optional)");

        notes = addInput(root,
                sinhala ? "අමතර සටහන්" : "Additional Notes (Optional)");

        statusText = makeText("",14,GOLD,false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0,dp(12),0,dp(8));
        root.addView(statusText,full());

        saveButton = makeButton(
                sinhala ? "හමුවීම Save කරන්න" : "SAVE APPOINTMENT",
                GOLD,NAVY);
        root.addView(saveButton,buttonParams());
        saveButton.setOnClickListener(v -> createAppointment());

        Button refresh = makeButton(
                sinhala ? "හමුවීම් නැවත ලබාගන්න" : "REFRESH APPOINTMENTS",
                Color.rgb(49,69,94),WHITE);
        root.addView(refresh,buttonParams());
        refresh.setOnClickListener(v -> loadAppointments());

        addHeading(root,sinhala ? "මගේ හමුවීම්" : "MY APPOINTMENT LIST");

        appointmentList = new LinearLayout(this);
        appointmentList.setOrientation(LinearLayout.VERTICAL);
        root.addView(appointmentList,full());

        setContentView(scroll);
    }

    private TextView makeText(String value,int size,int color,boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if(bold) t.setTypeface(null,Typeface.BOLD);
        return t;
    }

    private void addHeading(LinearLayout root,String value) {
        TextView t = makeText(value,15,GOLD,true);
        t.setPadding(0,dp(18),0,dp(9));
        root.addView(t,full());
    }

    private EditText addInput(LinearLayout root,String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(false);
        e.setTextColor(WHITE);
        e.setHintTextColor(Color.LTGRAY);
        e.setHint(hint);
        e.setTextSize(15);
        e.setPadding(dp(12),dp(10),dp(12),dp(10));
        e.setBackgroundTintList(ColorStateList.valueOf(GOLD));

        LinearLayout.LayoutParams p = full();
        p.bottomMargin = dp(8);
        root.addView(e,p);
        return e;
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
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp(52));
        p.topMargin = dp(7);
        p.bottomMargin = dp(7);
        return p;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void chooseDate() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view,year,month,day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year,month,day);
                    selectedDate = new SimpleDateFormat(
                            "yyyy-MM-dd",Locale.US).format(selected.getTime());
                    dateText.setText(selectedDate);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void chooseTime() {
        Calendar c = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view,hour,minute) -> {
                    selectedTime = String.format(
                            Locale.US,"%02d:%02d",hour,minute);
                    timeText.setText(selectedTime);
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void createAppointment() {

        String name = customer.getText().toString().trim();
        String mobile = phone.getText().toString().trim();
        String purpose = task.getText().toString().trim();

        if(name.isEmpty()) {
            customer.setError(sinhala ? "නම ඇතුළත් කරන්න" : "Enter customer name");
            return;
        }

        if(mobile.isEmpty()) {
            phone.setError(sinhala ? "දුරකථන අංකය ඇතුළත් කරන්න" : "Enter phone number");
            return;
        }

        if(purpose.isEmpty()) {
            task.setError(sinhala ? "කාර්යය ඇතුළත් කරන්න" : "Enter task");
            return;
        }

        if(selectedDate.isEmpty()) {
            statusText.setText(sinhala ? "දිනය තෝරන්න." : "Please select a date.");
            return;
        }

        if(selectedTime.isEmpty()) {
            statusText.setText(sinhala ? "වේලාව තෝරන්න." : "Please select a time.");
            return;
        }

        saveButton.setEnabled(false);
        statusText.setText(sinhala ? "හමුවීම Save කරමින්..." : "Saving appointment...");

        executor.execute(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action","createAppointment");
                data.put("sessionToken",token);
                data.put("customerName",name);
                data.put("phoneNumber",mobile);
                data.put("task",purpose);
                data.put("appointmentDate",selectedDate);
                data.put("appointmentTime",selectedTime);
                data.put("location",location.getText().toString().trim());
                data.put("notes",notes.getText().toString().trim());

                JSONObject result = post(data);

                runOnUiThread(() -> {
                    saveButton.setEnabled(true);

                    if(result.optBoolean("success",false)) {
                        statusText.setText(result.optString("message",
                                sinhala ? "හමුවීම සාර්ථකව Save කළා." :
                                        "Appointment saved successfully."));

                        customer.setText("");
                        phone.setText("");
                        task.setText("");
                        location.setText("");
                        notes.setText("");
                        selectedDate = "";
                        selectedTime = "";
                        dateText.setText(sinhala ? "දිනය තෝරා නැත" : "No date selected");
                        timeText.setText(sinhala ? "වේලාව තෝරා නැත" : "No time selected");

                        loadAppointments();
                    } else {
                        statusText.setText(result.optString("message",
                                sinhala ? "Save කිරීමට නොහැකි වුණා." :
                                        "Could not save appointment."));
                    }
                });

            } catch(Exception e) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    statusText.setText(sinhala ?
                            "සම්බන්ධතාවයේ දෝෂයක්. නැවත උත්සාහ කරන්න." :
                            "Connection error. Please try again.");
                });
            }
        });
    }

    private void loadAppointments() {
        statusText.setText(sinhala ? "හමුවීම් ලබාගනිමින්..." : "Loading appointments...");

        executor.execute(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action","getMyAppointments");
                data.put("sessionToken",token);
                data.put("fromDate","");
                data.put("toDate","");

                JSONObject result = post(data);

                runOnUiThread(() -> showAppointments(result));

            } catch(Exception e) {
                runOnUiThread(() ->
                        statusText.setText(sinhala ?
                                "හමුවීම් ලබාගැනීමට නොහැකි වුණා." :
                                "Could not load appointments."));
            }
        });
    }

    private void showAppointments(JSONObject result) {

        appointmentList.removeAllViews();

        if(!result.optBoolean("success",false)) {
            statusText.setText(result.optString("message",
                    sinhala ? "දත්ත ලබාගැනීම අසාර්ථකයි." : "Request failed."));
            return;
        }

        JSONArray array = result.optJSONArray("appointments");

        if(array == null || array.length() == 0) {
            statusText.setText(sinhala ?
                    "දැනට හමුවීම් කිසිවක් නැහැ." :
                    "No appointments found.");
            return;
        }

        statusText.setText(sinhala ?
                "හමුවීම් ගණන: " + array.length() :
                "Total appointments: " + array.length());

        for(int i=0;i<array.length();i++) {
            JSONObject item = array.optJSONObject(i);
            if(item == null) continue;

            addAppointmentCard(item);
        }
    }

    private void addAppointmentCard(JSONObject item) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(12),dp(14),dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(37,51,70));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);

        LinearLayout.LayoutParams cp = full();
        cp.bottomMargin = dp(12);
        appointmentList.addView(card,cp);

        String name = item.optString("customerName","");
        String mobile = item.optString("phoneNumber","");
        String purpose = item.optString("task","");
        String date = item.optString("appointmentDate","");
        String time = item.optString("appointmentTime","");
        String place = item.optString("location","");
        String note = item.optString("notes","");
        String state = item.optString("status","SCHEDULED");
        String id = item.optString("appointmentID","");

        addCardText(card,name,17,GOLD,true);
        addCardText(card,(sinhala ? "දුරකථනය: " : "Phone: ")+mobile,14,WHITE,false);
        addCardText(card,(sinhala ? "කාර්යය: " : "Task: ")+purpose,14,WHITE,false);
        addCardText(card,(sinhala ? "දිනය: " : "Date: ")+date,14,WHITE,false);
        addCardText(card,(sinhala ? "වේලාව: " : "Time: ")+time,14,WHITE,false);

        if(!place.isEmpty())
            addCardText(card,(sinhala ? "ස්ථානය: " : "Location: ")+place,14,WHITE,false);

        if(!note.isEmpty())
            addCardText(card,(sinhala ? "සටහන්: " : "Notes: ")+note,14,WHITE,false);

        addCardText(card,(sinhala ? "තත්ත්වය: " : "Status: ")+state,14,GOLD,true);

        if(state.equalsIgnoreCase("SCHEDULED")) {
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0,dp(8),0,0);
            card.addView(actions,full());

            Button complete = makeButton(
                    sinhala ? "සම්පූර්ණයි" : "COMPLETED",
                    Color.rgb(48,130,88),WHITE);

            Button cancel = makeButton(
                    sinhala ? "අවලංගු කරන්න" : "CANCEL",
                    Color.rgb(155,65,65),WHITE);

            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0,dp(46),1);
            ap.rightMargin=dp(5);
            actions.addView(complete,ap);

            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,dp(46),1);
            bp.leftMargin=dp(5);
            actions.addView(cancel,bp);

            complete.setOnClickListener(v ->
                    updateAppointment(id,"COMPLETED"));

            cancel.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setMessage(sinhala ? "මෙම හමුවීම අවලංගු කරනවාද?" :
                                    "Cancel this appointment?")
                            .setNegativeButton(sinhala ? "නැහැ" : "NO",null)
                            .setPositiveButton(sinhala ? "ඔව්" : "YES",
                                    (d,w) -> updateAppointment(id,"CANCELLED"))
                            .show());
        }
    }

    private void addCardText(LinearLayout card,String value,
                             int size,int color,boolean bold) {
        TextView t=makeText(value,size,color,bold);
        t.setPadding(0,dp(3),0,dp(3));
        card.addView(t,full());
    }

    private void updateAppointment(String id,String newStatus) {

        statusText.setText(sinhala ? "යාවත්කාලීන කරමින්..." : "Updating appointment...");

        executor.execute(() -> {
            try {
                JSONObject data=new JSONObject();
                data.put("action","updateMyAppointment");
                data.put("sessionToken",token);
                data.put("appointmentID",id);
                data.put("status",newStatus);

                JSONObject result=post(data);

                runOnUiThread(() -> {
                    statusText.setText(result.optString("message",
                            sinhala ? "යාවත්කාලීන කිරීම අවසන්." : "Update finished."));
                    loadAppointments();
                });

            } catch(Exception e) {
                runOnUiThread(() ->
                        statusText.setText(sinhala ?
                                "යාවත්කාලීන කිරීමට නොහැකි වුණා." :
                                "Could not update appointment."));
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

            try(OutputStream out=connection.getOutputStream()) {
                out.write(data.toString().getBytes("UTF-8"));
            }

            int code=connection.getResponseCode();

            InputStream input=(code>=200 && code<400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if(input==null) throw new Exception("Empty response: "+code);

            StringBuilder response=new StringBuilder();

            try(BufferedReader reader=new BufferedReader(
                    new InputStreamReader(input,"UTF-8"))) {
                String line;
                while((line=reader.readLine())!=null)
                    response.append(line);
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
