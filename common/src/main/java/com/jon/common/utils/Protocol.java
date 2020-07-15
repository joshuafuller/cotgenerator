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
        final String protocolString = PrefUtils.getString(prefs, Key.TRANSMISSION_PROTOCOL);
        return fromString(protocolString);
    }

    public static Protocol fromString(String protocolString) {
        for (Protocol protocol : values()) {
            if (protocol.get().equals(protocolString)) {
                return protocol;
            }
        }
        throw new IllegalArgumentException("Unknown protocol: " + protocolString);
    }
}
