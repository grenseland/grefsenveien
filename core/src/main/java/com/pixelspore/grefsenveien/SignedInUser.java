package com.pixelspore.grefsenveien;

import android.content.Context;

import androidx.annotation.Nullable;

/** Signed-in user lookups shared by the phone, Android Auto and TV surfaces. */
public final class SignedInUser {

    private static final String PREFS_NAME = "GrefsenveienPrefs";
    private static final String KEY_USER_EMAIL = "user_email";

    /** Only this user sees when motion was last detected in a room. */
    private static final String MOTION_TIMES_EMAIL = "thomas.ermesjo@gmail.com";

    private SignedInUser() {}

    @Nullable
    public static String email(@Nullable Context context) {
        if (context == null) return null;
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_EMAIL, null);
    }

    public static boolean canSeeMotionTimes(@Nullable Context context) {
        String email = email(context);
        return email != null && MOTION_TIMES_EMAIL.equalsIgnoreCase(email.trim());
    }
}
