package com.pixelspore.grefsenveien;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** User-toggleable dashboard settings shared by the phone and Android Auto surfaces. */
public final class DashboardSettings {

    private static final String PREFS_NAME = "GrefsenveienPrefs";

    /** Debug mode adds the light dots and motion times to the room cards. */
    private static final String KEY_DEBUG_MODE = "debug_mode";

    private DashboardSettings() {}

    /** Whether debug mode is actually in effect, which requires a user allowed to turn it on. */
    public static boolean isDebugModeEnabled(@Nullable Context context) {
        return SignedInUser.canUseDebugMode(context) && isDebugModeSelected(context);
    }

    /** The stored setting, regardless of who is signed in. Use this to render the settings UI. */
    public static boolean isDebugModeSelected(@Nullable Context context) {
        if (context == null) return false;
        return prefs(context).getBoolean(KEY_DEBUG_MODE, false);
    }

    public static void setDebugMode(@NonNull Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DEBUG_MODE, enabled).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
