package com.shreeadvertising.workers;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MyClientsActivity extends Activity {

    private static final String API_URL = "https://script.google.com/macros/s/AKfycbzyR9xjOZXQJ0VLqhrGJWgtjKp50fduF9ZyIc6xv-HlwJJUQqo4b9F8e3YAnDXFxOai7A/exec";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final int NAVY = Color.rgb(16,27,45);
    private final int GOLD = Color.rgb(214,173,85);
    private final int WHITE = Color.WHITE;

    private String token;
    private boolean sinhala;
    private String editingClientID = "";

    private EditText nameInput, phoneInput, addressInput, notesInput, searchInput;
    private TextView statusText;
    private LinearLayout clientList;
    private Button saveButton;

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
            loadClients("");
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
                sinhala ? "මගේ පාරිභෝගිකයන්" : "MY CLIENTS",
                23,GOLD,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title,full());

        addHeading(root,sinhala ? "පාරිභෝගික තොරතුරු" : "CLIENT DETAILS");

        nameInput = addInput(root,sinhala ? "සම්පූර්ණ නම" : "Client Full Name");
        phoneInput = addInput(root,sinhala ? "දුරකථන අංකය" : "Phone Number");
        addressInput = addInput(root,sinhala ? "සේවා ස්ථානය / ලිපිනය" : "Workplace Address (Optional)");
        notesInput = addInput(root,sinhala ? "අමතර සටහන්" : "Notes (Optional)");

        saveButton = makeButton(
                sinhala ? "පාරිභෝගිකයා Save කරන්න" : "SAVE CLIENT",
                GOLD,NAVY);
        root.addView(saveButton,buttonParams());
        saveButton.setOnClickListener(v -> saveClient());

        Button clearButton = makeButton(
                sinhala ? "අලුත් පාරිභෝගිකයෙක්" : "NEW CLIENT / CLEAR",
                Color.rgb(49,69,94),WHITE);
        root.addView(clearButton,buttonParams());
        clearButton.setOnClickListener(v -> clearForm());

        addHeading(root,sinhala ? "පාරිභෝගිකයන් සොයන්න" : "SEARCH CLIENTS");

        searchInput = addInput(root,sinhala ? "නම / දුරකථන අංකය" : "Name / Phone Number");
        Button searchButton = makeButton(
                sinhala ? "සොයන්න" : "SEARCH",
                GOLD,NAVY);
        root.addView(searchButton,buttonParams());
        searchButton.setOnClickListener(v ->
                loadClients(searchInput.getText().toString().trim()));

        Button refreshButton = makeButton(
                sinhala ? "සියලුම පාරිභෝගිකයන්" : "SHOW ALL CLIENTS",
                Color.rgb(49,69,94),WHITE);
        root.addView(refreshButton,buttonParams());
        refreshButton.setOnClickListener(v -> {
            searchInput.setText("");
            loadClients("");
        });

        statusText = makeText("",14,GOLD,false);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0,dp(14),0,dp(12));
        root.addView(statusText,full());

        addHeading(root,sinhala ? "මගේ පාරිභෝගික ලැයිස්තුව" : "MY CLIENT LIST");

        clientList = new LinearLayout(this);
        clientList.setOrientation(LinearLayout.VERTICAL);
        root.addView(clientList,full());

        setContentView(scroll);
    }

    private TextView makeText(String value,int size,int color,boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null,Typeface.BOLD);
        return t;
    }

    private void addHeading(LinearLayout root,String value) {
        TextView t = makeText(value,15,GOLD,true);
        t.setPadding(0,dp(17),0,dp(9));
        root.addView(t,full());
    }

    private EditText addInput(LinearLayout root,String hint) {
        EditText e = new EditText(this);
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
        p.topMargin = dp(6);
        p.bottomMargin = dp(6);
        return p;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void clearForm() {
        editingClientID = "";
        nameInput.setText("");
        phoneInput.setText("");
        addressInput.setText("");
        notesInput.setText("");
        saveButton.setText(sinhala ? "පාරිභෝගිකයා Save කරන්න" : "SAVE CLIENT");
        statusText.setText(sinhala ? "අලුත් පාරිභෝගිකයෙක් ඇතුළත් කරන්න." : "Ready for a new client.");
    }

    private void saveClient() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError(sinhala ? "නම ඇතුළත් කරන්න." : "Enter client name.");
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError(sinhala ? "දුරකථන අංකය ඇතුළත් කරන්න." : "Enter phone number.");
            return;
        }

        saveButton.setEnabled(false);
        statusText.setText(sinhala ? "දත්ත Save කරමින්..." : "Saving client...");

        executor.execute(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action", editingClientID.isEmpty()
                        ? "createClient" : "updateMyClient");
                data.put("sessionToken",token);
                data.put("clientFullName",name);
                data.put("phoneNumber",phone);
                data.put("workplaceAddress",addressInput.getText().toString().trim());
                data.put("notes",notesInput.getText().toString().trim());

                if (!editingClientID.isEmpty()) {
                    data.put("clientID",editingClientID);
                }

                JSONObject result = post(data);

                runOnUiThread(() -> {
                    saveButton.setEnabled(true);

                    if (result.optBoolean("success",false)) {
                        statusText.setText(result.optString("message",
                                sinhala ? "සාර්ථකයි." : "Saved successfully."));
                        clearForm();
                        loadClients("");
                    } else {
                        statusText.setText(result.optString("message",
                                sinhala ? "දත්ත Save කිරීමට නොහැකි වුණා." :
                                        "Could not save client."));
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    statusText.setText(sinhala ?
                            "සම්බන්ධතාවයේ දෝෂයක්." :
                            "Connection error. Please try again.");
                });
            }
        });
    }

    private void loadClients(String search) {
        statusText.setText(sinhala ? "පාරිභෝගිකයන් ලබාගනිමින්..." : "Loading clients...");

        executor.execute(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action","getMyClients");
                data.put("sessionToken",token);
                data.put("search",search);

                JSONObject result = post(data);
                runOnUiThread(() -> showClients(result));

            } catch (Exception e) {
                runOnUiThread(() -> statusText.setText(
                        sinhala ? "දත්ත ලබාගැනීමට නොහැකි වුණා." :
                                "Could not load clients."));
            }
        });
    }

    private void showClients(JSONObject result) {
        clientList.removeAllViews();

        if (!result.optBoolean("success",false)) {
            statusText.setText(result.optString("message",
                    sinhala ? "දත්ත ලබාගැනීම අසාර්ථකයි." : "Request failed."));
            return;
        }

        JSONArray clients = result.optJSONArray("clients");

        if (clients == null || clients.length() == 0) {
            statusText.setText(sinhala ?
                    "පාරිභෝගිකයන් කිසිවෙක් හමු වුණේ නැහැ." :
                    "No clients found.");
            return;
        }

        statusText.setText(sinhala ?
                "පාරිභෝගිකයන් ගණන: " + clients.length() :
                "Total clients: " + clients.length());

        for (int i=0; i<clients.length(); i++) {
            JSONObject item = clients.optJSONObject(i);
            if (item != null) addClientCard(item);
        }
    }

    private void addClientCard(JSONObject item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(12),dp(14),dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(37,51,70));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);

        LinearLayout.LayoutParams cp = full();
        cp.bottomMargin = dp(12);
        clientList.addView(card,cp);

        String id = item.optString("clientID","");
        String name = item.optString("clientFullName","");
        String phone = item.optString("phoneNumber","");
        String address = item.optString("workplaceAddress","");
        String notes = item.optString("notes","");

        addCardText(card,name,17,GOLD,true);
        addCardText(card,(sinhala ? "දුරකථනය: " : "Phone: ")+phone,14,WHITE,false);

        if (!address.isEmpty())
            addCardText(card,(sinhala ? "ලිපිනය: " : "Address: ")+address,14,WHITE,false);

        if (!notes.isEmpty())
            addCardText(card,(sinhala ? "සටහන්: " : "Notes: ")+notes,14,WHITE,false);

        Button edit = makeButton(
                sinhala ? "Edit / වෙනස් කරන්න" : "EDIT CLIENT",
                GOLD,NAVY);
        LinearLayout.LayoutParams ep = buttonParams();
        ep.topMargin = dp(10);
        card.addView(edit,ep);

        edit.setOnClickListener(v -> {
            editingClientID = id;
            nameInput.setText(name);
            phoneInput.setText(phone);
            addressInput.setText(address);
            notesInput.setText(notes);
            saveButton.setText(sinhala ? "වෙනස්කම් Update කරන්න" : "UPDATE CLIENT");
            statusText.setText(sinhala ?
                    "දැන් පාරිභෝගික තොරතුරු වෙනස් කළ හැකියි." :
                    "Edit the client details and press Update.");
            nameInput.requestFocus();
        });
    }

    private void addCardText(LinearLayout card,String value,
                             int size,int color,boolean bold) {
        TextView t = makeText(value,size,color,bold);
        t.setPadding(0,dp(3),0,dp(3));
        card.addView(t,full());
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

            try(OutputStream out = connection.getOutputStream()) {
                out.write(data.toString().getBytes("UTF-8"));
            }

            int code = connection.getResponseCode();
            InputStream input = (code >= 200 && code < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (input == null) throw new Exception("Empty response: " + code);

            StringBuilder response = new StringBuilder();

            try(BufferedReader reader = new BufferedReader(
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
