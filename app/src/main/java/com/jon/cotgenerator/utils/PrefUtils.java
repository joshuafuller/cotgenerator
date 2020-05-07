package com.jon.cotgenerator.utils;

import android.content.SharedPreferences;

/* Some QoL utility methods for preferences that are known to be valid beforehand */
public final class PrefUtils {
    private PrefUtils() {
    }

    public static double parseDouble(final SharedPreferences prefs, final String key) {
        try {
            return Double.parseDouble(prefs.getString(key, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int parseInt(final SharedPreferences prefs, final String key) {
        try {
            return Integer.parseInt(prefs.getString(key, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getInt(final SharedPreferences prefs, final String key) {
        try {
            return prefs.getInt(key, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getString(final SharedPreferences prefs, final String key) {
        try {
            return prefs.getString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean getBoolean(final SharedPreferences prefs, final String key) {
        try {
            return prefs.getBoolean(key, true);
        } catch (Exception e) {
            return false;
        }
    }
}
