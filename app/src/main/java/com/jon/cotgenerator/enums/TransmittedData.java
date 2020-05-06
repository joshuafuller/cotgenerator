package com.jon.cotgenerator.enums;

import android.content.SharedPreferences;

import com.jon.cotgenerator.utils.Key;

public enum TransmittedData {
    GPS,
    FAKE;

    public static TransmittedData fromPrefs(SharedPreferences prefs) {
        String choice = prefs.getString(Key.TRANSMITTED_DATA, "");
        switch (choice) {
            case "GPS Position":
                return GPS;
            case "Fake Icons":
                return FAKE;
            default:
                throw new IllegalArgumentException("Unknown data type: " + choice);
        }

    }
}
