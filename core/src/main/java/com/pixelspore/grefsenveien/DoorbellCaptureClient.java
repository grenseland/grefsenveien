package com.pixelspore.grefsenveien;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Triggers a fresh yard/doorbell camera capture via {@code DOORBELL_TAKE_IMAGE_URL}.
 */
public final class DoorbellCaptureClient {

    private static final String TAG = "GrefsenveienApp";

    private DoorbellCaptureClient() {}

    public static void triggerCapture(@Nullable String captureUrl, @Nullable String authToken) {
        if (captureUrl == null || captureUrl.isEmpty()) {
            return;
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(captureUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                applyAuthorization(connection, authToken);
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    Log.w(TAG, "Doorbell capture trigger failed: HTTP " + responseCode);
                }
            } catch (Exception e) {
                Log.w(TAG, "Doorbell capture trigger failed", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }, "DoorbellCapture").start();
    }

    static void applyAuthorization(@NonNull HttpURLConnection connection, @Nullable String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            return;
        }
        connection.setRequestProperty("Authorization", authToken);
    }
}
