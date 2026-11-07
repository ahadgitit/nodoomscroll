package com.nodoomscroll.shorts;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around the app's two saved values. */
final class Prefs {
    private static final String FILE = "nodoomscroll";
    private static final String KEY_PAUSED = "paused";
    private static final String KEY_BLOCKED = "blocked_count";

    private Prefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean isPaused(Context c) { return sp(c).getBoolean(KEY_PAUSED, false); }

    static void setPaused(Context c, boolean paused) { sp(c).edit().putBoolean(KEY_PAUSED, paused).apply(); }

    static int blockedCount(Context c) { return sp(c).getInt(KEY_BLOCKED, 0); }

    static void incrementBlocked(Context c) {
        sp(c).edit().putInt(KEY_BLOCKED, blockedCount(c) + 1).apply();
    }
}
