package com.jon.cotgenerator.enums;

import android.content.SharedPreferences;

import com.jon.cotgenerator.utils.Key;

public enum TransmittedData {
    GPS("GPS Position"),
    FAKE("Fake Icons");

    private final String name;

    TransmittedData(String name) { this.name = name; }

    public String get() { return name; }

    public static TransmittedData fromPrefs(SharedPreferences prefs) {
        String choice = prefs.getString(Key.TRANSMITTED_DATA, "");
        switch (choice) {
            case "GPS Position": return GPS;
            case "Fake Icons": return FAKE;
            default: throw new IllegalArgumentException("Unknown data type: " + choice);
        }

    }
}
