package com.pixelspore.grefsenveien.tv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.pixelspore.grefsenveien.DoorbellCaptureClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TvMainActivity extends AppCompatActivity {

    private static final String PREFS = "GrefsenveienPrefs";
    private static final long DOORBELL_INTERVAL_MS = 120_000L;

    private SharedPreferences prefs;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    private TvDashboardView dashboard;
    private TextView tvStatus;
    private TextView tvLoggedInAs;
    private Button btnGarage;
    private Button btnGaragePlus;
    private Button btnGate;
    private Button btnLogout;

    private String garageStatus = "";
    private String gateStatus = "";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable doorbellUpdater = new Runnable() {
        @Override
        public void run() {
            triggerDoorbellCapture();
            handler.postDelayed(this, DOORBELL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        dashboard = findViewById(R.id.dashboard);
        tvStatus = findViewById(R.id.tvStatus);
        tvLoggedInAs = findViewById(R.id.tvLoggedInAs);
        btnGarage = findViewById(R.id.btnGarage);
        btnGaragePlus = findViewById(R.id.btnGaragePlus);
        btnGate = findViewById(R.id.btnGate);
        btnLogout = findViewById(R.id.btnLogout);

        btnGarage.setOnClickListener(v -> makeUrlRequest("garasjen", null));
        btnGaragePlus.setOnClickListener(v -> makeUrlRequest("garasjen", 60));
        btnGate.setOnClickListener(v -> makeUrlRequest("porten", null));
        btnLogout.setOnClickListener(v -> handleLogout());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
                    } else if (result.getResultCode() == RESULT_OK) {
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                        if (account != null && account.getEmail() != null) {
                            prefs.edit().putString("user_email", account.getEmail()).apply();
                            updateLoggedInLabel(account.getEmail());
                            setControlsEnabled(true);
                        } else {
                            handler.postDelayed(this::checkLoginStatus, 1000);
                        }
                    }
                });

        btnGarage.requestFocus();
        checkLoginStatus();
        updateStatusText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dashboard.startUpdating();
        handler.removeCallbacks(doorbellUpdater);
        handler.post(doorbellUpdater);
        String email = prefs.getString("user_email", null);
        updateLoggedInLabel(email);
        setControlsEnabled(email != null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dashboard.stopUpdating();
        handler.removeCallbacks(doorbellUpdater);
    }

    private void checkLoginStatus() {
        String savedEmail = prefs.getString("user_email", null);
        if (savedEmail == null) {
            setControlsEnabled(false);
            updateLoggedInLabel(null);
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        } else {
            updateLoggedInLabel(savedEmail);
            setControlsEnabled(true);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getEmail() != null) {
                prefs.edit().putString("user_email", account.getEmail()).apply();
                updateLoggedInLabel(account.getEmail());
                setControlsEnabled(true);
            }
        } catch (ApiException e) {
            updateLoggedInLabel(null);
            setControlsEnabled(false);
            tvStatus.setText(R.string.tv_login_prompt);
        }
    }

    private void handleLogout() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                prefs.edit().remove("user_email").apply();
                updateLoggedInLabel(null);
                setControlsEnabled(false);
                checkLoginStatus();
            });
        }
    }

    private void updateLoggedInLabel(@Nullable String email) {
        if (email == null) {
            tvLoggedInAs.setText(R.string.tv_not_logged_in);
        } else {
            tvLoggedInAs.setText(getString(R.string.tv_logged_in_as, email));
        }
    }

    private void setControlsEnabled(boolean enabled) {
        btnGarage.setEnabled(enabled);
        btnGaragePlus.setEnabled(enabled);
        btnGate.setEnabled(enabled);
        float alpha = enabled ? 1f : 0.45f;
        btnGarage.setAlpha(alpha);
        btnGaragePlus.setAlpha(alpha);
        btnGate.setAlpha(alpha);
    }

    private void updateStatusText() {
        StringBuilder sb = new StringBuilder();
        if (!garageStatus.isEmpty()) {
            sb.append("Garasje: ").append(garageStatus);
        }
        if (!gateStatus.isEmpty()) {
            if (sb.length() > 0) sb.append("   ·   ");
            sb.append("Port: ").append(gateStatus);
        }
        tvStatus.setText(sb.toString());
    }

    private void makeUrlRequest(String targetName, @Nullable Integer delaySeconds) {
        if (prefs.getString("user_email", null) == null) {
            checkLoginStatus();
            return;
        }

        if ("garasjen".equals(targetName)) {
            garageStatus = delaySeconds != null
                    ? "Sender melding (+" + delaySeconds + "s)..."
                    : "Sender melding...";
        } else {
            gateStatus = "Sender melding...";
        }
        updateStatusText();

        new Thread(() -> {
            try {
                URL url = new URL("garasjen".equals(targetName)
                        ? BuildConfig.GARAGE_WEBHOOK_URL
                        : BuildConfig.GATE_WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String userEmail = prefs.getString("user_email", "unknown");
                StringBuilder payload = new StringBuilder();
                payload.append("{\"token\":\"Xi3gQF4GTFR7aENMkMjftt4P\",\"user\":\"")
                        .append(userEmail).append("\"");
                if (delaySeconds != null && "garasjen".equals(targetName)) {
                    payload.append(",\"delay\":").append(delaySeconds);
                }
                payload.append("}");

                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                runOnUiThread(() -> {
                    String finalTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    String resultMsg = responseCode == 200
                            ? "Sendt kl " + finalTime
                            : "Feil (Kode " + responseCode + ") kl " + finalTime;
                    if ("garasjen".equals(targetName)) {
                        garageStatus = resultMsg;
                    } else {
                        gateStatus = resultMsg;
                    }
                    updateStatusText();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    String errorTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    if ("garasjen".equals(targetName)) {
                        garageStatus = "Nettverksfeil kl " + errorTime;
                    } else {
                        gateStatus = "Nettverksfeil kl " + errorTime;
                    }
                    updateStatusText();
                });
            }
        }, "TvWebhook").start();
    }

    private void triggerDoorbellCapture() {
        DoorbellCaptureClient.triggerCapture(
                BuildConfig.DOORBELL_TAKE_IMAGE_URL,
                BuildConfig.DOORBELL_TAKE_IMAGE_TOKEN);
    }
}
