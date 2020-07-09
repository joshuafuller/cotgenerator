package com.jon.cot.generator.utils;

import android.content.SharedPreferences;

public enum DataFormat {
    XML("XML"),
    PROTOBUF("Protobuf");

    private final String protocol;

    DataFormat(String protocol) {
        this.protocol = protocol;
    }

    public String get() {
        return protocol;
    }

    public static DataFormat fromPrefs(SharedPreferences prefs) {
        return fromString(prefs.getString(Key.DATA_FORMAT, ""));
    }

    public static DataFormat fromString(String str) {
        switch (str) {
            case "XML": return XML;
            case "Protobuf": return PROTOBUF;
            default: throw new IllegalArgumentException("Unknown data format: " + str);
        }
    }
}
