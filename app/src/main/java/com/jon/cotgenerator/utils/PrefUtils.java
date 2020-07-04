package com.jon.cotgenerator.utils;

import android.content.SharedPreferences;

import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.enums.Protocol;

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

    public static String getPresetInfoString(final SharedPreferences prefs) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        String selectedPreset;
        switch (protocol) {
            case TCP: selectedPreset = getString(prefs, Key.TCP_PRESETS); break;
            case UDP: selectedPreset = getString(prefs, Key.UDP_PRESETS); break;
            default: throw new IllegalArgumentException("Unknown protocol: " + protocol.get());
        }
        OutputPreset preset = OutputPreset.fromString(selectedPreset);
        return protocol.get() + (preset == null ? ": Unknown" : ": " + preset.alias);
    }
}
