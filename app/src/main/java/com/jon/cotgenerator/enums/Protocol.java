package com.jon.cotgenerator.enums;

import android.content.SharedPreferences;

import com.jon.cotgenerator.utils.Key;

public enum Protocol {
    UDP("UDP"),
    TCP("TCP");

    private final String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    public String get() {
        return protocol;
    }

    public static Protocol fromPrefs(SharedPreferences prefs) {
        return fromString(prefs.getString(Key.TRANSMISSION_PROTOCOL, ""));
    }

    public static Protocol fromString(String str) {
        switch (str) {
            case "UDP": return UDP;
            case "TCP": return TCP;
            default: throw new IllegalArgumentException("Unknown transmission protocol: " + str);
        }
    }
}
