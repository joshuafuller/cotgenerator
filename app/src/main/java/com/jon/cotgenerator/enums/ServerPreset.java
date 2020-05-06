package com.jon.cotgenerator.enums;

import android.content.SharedPreferences;

import androidx.preference.EditTextPreference;

import com.jon.cotgenerator.utils.Key;

public enum ServerPreset {
    FREETAKSERVER("204.48.30.216", "8087"),
    TAKSERVER("54.189.86.157", "8088");

    private String address;
    private String port;

    ServerPreset(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public static ServerPreset fromPrefs(SharedPreferences prefs) {
        String choice = prefs.getString(Key.TRANSMISSION_PROTOCOL, "");
        switch (choice) {
            case "Public FreeTakServer":
                return FREETAKSERVER;
            case "Public TAK Server":
                return TAKSERVER;
            default:
                throw new IllegalArgumentException("Unknown server preset: " + choice);
        }
    }

    public void fillPreferences(EditTextPreference addrPref, EditTextPreference portPref) {
        addrPref.setText(this.address);
        portPref.setText(this.port);
    }
}
