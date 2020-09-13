package com.jon.common.utils

import android.content.SharedPreferences

enum class DataFormat(private val format: String) {
    XML("XML"),
    PROTOBUF("Protobuf");

    override fun toString(): String {
        return format
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences): DataFormat {
            val formatString = PrefUtils.getString(prefs, Key.DATA_FORMAT)
            return fromString(formatString)
        }

        fun fromString(formatString: String): DataFormat {
            return values().first { it.toString() == formatString }
        }
    }
}
