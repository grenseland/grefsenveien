package com.pixelspore.grefsenveien;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** User-toggleable dashboard settings shared by the phone and Android Auto surfaces. */
public final class DashboardSettings {

    private static final String PREFS_NAME = "GrefsenveienPrefs";

    /** Controls the light dots and motion times drawn on the room cards. */
    private static final String KEY_ROOM_DETAILS = "room_card_details";
    private static final boolean ROOM_DETAILS_DEFAULT = true;

    private DashboardSettings() {}

    public static boolean showRoomDetails(@Nullable Context context) {
        if (context == null) return ROOM_DETAILS_DEFAULT;
        return prefs(context).getBoolean(KEY_ROOM_DETAILS, ROOM_DETAILS_DEFAULT);
    }

    /** Flips the setting and returns the new value. */
    public static boolean toggleRoomDetails(@NonNull Context context) {
        boolean enabled = !showRoomDetails(context);
        prefs(context).edit().putBoolean(KEY_ROOM_DETAILS, enabled).apply();
        return enabled;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
