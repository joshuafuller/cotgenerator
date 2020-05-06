package com.jon.cotgenerator.enums;

import android.content.SharedPreferences;

import com.jon.cotgenerator.utils.Key;

public enum TransmissionProtocol {
    UDP,
    TCP;

    public static TransmissionProtocol fromPrefs(SharedPreferences prefs) {
        String choice = prefs.getString(Key.TRANSMISSION_PROTOCOL, "");
        switch (choice) {
            case "UDP": return UDP;
            case "TCP": return TCP;
            default: throw new IllegalArgumentException("Unknown transmission protocol: " + choice);
        }

    }
}
