package com.jon.common.utils;

import android.content.SharedPreferences;

public enum Protocol {
    SSL("SSL"),
    TCP("TCP"),
    UDP("UDP");

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
            case "SSL": return SSL;
            case "TCP": return TCP;
            case "UDP": return UDP;
            default: throw new IllegalArgumentException("Unknown transmission protocol: " + str);
        }
    }
}
