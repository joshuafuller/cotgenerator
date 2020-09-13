package com.jon.common.utils

import android.content.SharedPreferences

enum class Protocol(private val protocol: String) {
    SSL("SSL"),
    TCP("TCP"),
    UDP("UDP");

    override fun toString(): String {
        return protocol
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences): Protocol {
            val protocolString = PrefUtils.getString(prefs, Key.TRANSMISSION_PROTOCOL)
            return fromString(protocolString)
        }

        fun fromString(protocolString: String): Protocol {
            return values().firstOrNull { it.toString() == protocolString }
                    ?: throw IllegalArgumentException("Unknown protocol: $protocolString")
        }
    }
}
