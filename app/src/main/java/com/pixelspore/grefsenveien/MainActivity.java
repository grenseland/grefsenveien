package com.pixelspore.grefsenveien;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String garageStatus = "";
    private String gateStatus = "";
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        View btnGarage = findViewById(R.id.btnGarage);
        View btnGate = findViewById(R.id.btnGate);

        btnGarage.setOnClickListener(v -> makeUrlRequest("garasjen"));
        btnGate.setOnClickListener(v -> makeUrlRequest("porten"));
        
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder statusBuilder = new StringBuilder();
        if (!garageStatus.isEmpty()) {
            statusBuilder.append("Garasje: ").append(garageStatus);
        }
        if (!gateStatus.isEmpty()) {
            if (statusBuilder.length() > 0) {
                statusBuilder.append("\n");
            }
            statusBuilder.append("Port: ").append(gateStatus);
        }
        
        String combinedStatus = statusBuilder.length() > 0 ? statusBuilder.toString() : " ";
        statusText.setText(combinedStatus);
    }

    private void makeUrlRequest(String targetName) {
        // Update UI to show waiting state
        if (targetName.equals("garasjen")) {
            garageStatus = "Sender melding...";
        } else {
            gateStatus = "Sender melding...";
        }
        updateStatusText();

        // Make request in background thread
        new Thread(() -> {
            try {
                URL url;
                if (targetName.equals("garasjen")) {
                    url = new URL(BuildConfig.GARAGE_WEBHOOK_URL);
                } else {
                    url = new URL(BuildConfig.GATE_WEBHOOK_URL);
                }
                
                android.util.Log.i("GrefsenveienApp", "MainActivity (Phone) -> Calling webhook: " + url.toString());
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                if (targetName.equals("garasjen")) {
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    String payload = "{\"token\":\"Xi3gQF4GTFR7aENMkMjftt4P\",\"user\":\"thomas@gmail.com\"}";
                    try (java.io.OutputStream os = connection.getOutputStream()) {
                        byte[] input = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                } else {
                    connection.setRequestMethod("GET");
                    connection.connect();
                }

                // Get response code 
                int responseCode = connection.getResponseCode();
                connection.disconnect();

                // Update UI on main thread with result
                runOnUiThread(() -> {
                    String finalTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    String resultMsg;
                    if (responseCode == 200) {
                        resultMsg = "Sendt kl " + finalTime;
                    } else {
                        resultMsg = "Feil (Kode " + responseCode + ") kl " + finalTime;
                    }
                    
                    if (targetName.equals("garasjen")) {
                        garageStatus = resultMsg;
                    } else {
                        gateStatus = resultMsg;
                    }
                    updateStatusText();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    String errorTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    if (targetName.equals("garasjen")) {
                        garageStatus = "Nettverksfeil kl " + errorTime;
                    } else {
                        gateStatus = "Nettverksfeil kl " + errorTime;
                    }
                    updateStatusText();
                });
            }
        }).start();
    }
}
