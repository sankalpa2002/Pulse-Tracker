package com.example.pulsetracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session using SharedPreferences.
 * Stores login state so the user stays signed in across app restarts.
 */
public class SessionManager {

    private static final String PREF_NAME = "PulseTrackerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_DARK_MODE = "isDarkMode";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(int userId, String fullName, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setDarkMode(boolean isDark) {
        editor.putBoolean(KEY_DARK_MODE, isDark);
        editor.apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    /** Returns true if dark mode pref has been explicitly set */
    public boolean hasDarkModePref() {
        return prefs.contains(KEY_DARK_MODE);
    }

    public void logout() {
        boolean dark = isDarkMode();
        editor.clear();
        editor.putBoolean(KEY_DARK_MODE, dark);
        editor.apply();
    }
}
}
