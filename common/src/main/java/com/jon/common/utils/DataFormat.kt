package com.jon.common.utils

import android.content.SharedPreferences
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair

enum class DataFormat(private val format: String) {
    XML("XML"),
    PROTOBUF("Protobuf");

    override fun toString(): String {
        return format
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences): DataFormat {
            return fromString(
                    prefs.getStringFromPair(CommonPrefs.DATA_FORMAT)
            )
        }

        fun fromString(formatString: String): DataFormat {
            return values().first { it.toString() == formatString }
        }
    }
}
