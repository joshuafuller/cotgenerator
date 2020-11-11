package com.jon.common.ui.location

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import java.lang.IllegalArgumentException

enum class CoordinateFormat {
    DD, DM, DMS, MGRS;

    companion object {
        @SuppressLint("DefaultLocale")
        fun fromString(str: String): CoordinateFormat {
            return values().firstOrNull { str.toLowerCase() == it.name.toLowerCase() }
                    ?: throw IllegalArgumentException("Unknown coordinate format '$str'")
        }

        fun fromPrefs(prefs: SharedPreferences): CoordinateFormat {
            return fromString(
                    prefs.getStringFromPair(CommonPrefs.LOCATION_COORDINATE_FORMAT)
            )
        }

        fun getNext(format: CoordinateFormat): CoordinateFormat {
            val index = values().indexOf(format)
            return if (index == values().size - 1) values()[0] else values()[index + 1]
        }
    }
}