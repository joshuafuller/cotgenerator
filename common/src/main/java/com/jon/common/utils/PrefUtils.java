package com.jon.common.utils;

import android.content.SharedPreferences;

import com.jon.common.presets.OutputPreset;

/* Some QoL utility methods for preferences that are known to be valid beforehand */
public final class PrefUtils {
    public static final int INVALID_INT_KEY = -1;

    private PrefUtils() {
    }

    public static double parseDouble(final SharedPreferences prefs, final String key) {
        try {
            return Double.parseDouble(prefs.getString(key, ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static int parseInt(final SharedPreferences prefs, final String key) {
        try {
            return Integer.parseInt(prefs.getString(key, ""));
        } catch (NumberFormatException e) {
            return INVALID_INT_KEY;
        }
    }

    public static int getInt(final SharedPreferences prefs, final String key) {
        try {
            return prefs.getInt(key, 0);
        } catch (Exception e) {
            return INVALID_INT_KEY;
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
            return prefs.getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getPresetInfoString(final SharedPreferences prefs) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        String selectedPreset;
        switch (protocol) {
            case SSL: selectedPreset = getString(prefs, Key.SSL_PRESETS); break;
            case TCP: selectedPreset = getString(prefs, Key.TCP_PRESETS); break;
            case UDP: selectedPreset = getString(prefs, Key.UDP_PRESETS); break;
            default: throw new IllegalArgumentException("Unknown protocol: " + protocol.get());
        }
        OutputPreset preset = OutputPreset.fromString(selectedPreset);
        return protocol.get() + (preset == null ? ": Unknown" : ": " + preset.alias);
    }

    public static String getPresetPrefKeyFromSharedPrefs(SharedPreferences prefs) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        switch (protocol) {
            case SSL: return Key.SSL_PRESETS;
            case TCP: return Key.TCP_PRESETS;
            case UDP: return Key.UDP_PRESETS;
            default: throw new IllegalArgumentException("Unknown protocol: " + protocol.get());
        }
    }
}
