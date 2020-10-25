package com.jon.common.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.PrefPair
import com.jon.common.prefs.getStringFromPair

enum class Protocol(
        private val protocol: String,
        val presetPref: PrefPair<String>
) {
    SSL(
            protocol = "SSL",
            presetPref = CommonPrefs.SSL_PRESETS
    ),
    TCP(
            protocol = "TCP",
            presetPref = CommonPrefs.TCP_PRESETS
    ),
    UDP(
            protocol = "UDP",
            presetPref = CommonPrefs.UDP_PRESETS
    );

    override fun toString(): String {
        return protocol
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences): Protocol {
            val protocolString = prefs.getStringFromPair(CommonPrefs.TRANSMISSION_PROTOCOL)
            return fromString(protocolString)
        }

        @SuppressLint("DefaultLocale")
        fun fromString(protocolString: String): Protocol {
            return values().firstOrNull { it.toString().toLowerCase() == protocolString.toLowerCase() }
                    ?: throw IllegalArgumentException("Unknown protocol: $protocolString")
        }
    }
}
