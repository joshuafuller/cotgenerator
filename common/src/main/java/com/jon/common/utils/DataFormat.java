package com.jon.common.utils;

import android.content.SharedPreferences;

public enum DataFormat {
    XML("XML"),
    PROTOBUF("Protobuf");

    private final String format;

    DataFormat(String format) {
        this.format = format;
    }

    public String get() {
        return format;
    }

    public static DataFormat fromPrefs(SharedPreferences prefs) {
        final String formatString = PrefUtils.getString(prefs, Key.DATA_FORMAT);
        return fromString(formatString);
    }

    public static DataFormat fromString(String formatString) {
        for (DataFormat format : values()) {
            if (format.get().equals(formatString)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown data format: " + formatString);
    }
}
