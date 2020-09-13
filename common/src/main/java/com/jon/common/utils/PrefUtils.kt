package com.jon.common.utils

import android.content.SharedPreferences
import com.jon.common.presets.OutputPreset

object PrefUtils {
    fun parseDouble(prefs: SharedPreferences, key: String): Double {
        return try {
            prefs.getString(key, "")!!.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun parseInt(prefs: SharedPreferences, key: String): Int {
        return try {
            prefs.getString(key, "")!!.toInt()
        } catch (e: NumberFormatException) {
            -1
        }
    }

    fun getInt(prefs: SharedPreferences, key: String): Int {
        return try {
            prefs.getInt(key, 0)
        } catch (e: Exception) {
            -1
        }
    }

    fun getString(prefs: SharedPreferences, key: String): String {
        return try {
            prefs.getString(key, "") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getBoolean(prefs: SharedPreferences, key: String): Boolean {
        return try {
            prefs.getBoolean(key, false)
        } catch (e: Exception) {
            false
        }
    }

    fun getPresetInfoString(prefs: SharedPreferences): String {
        val protocol = Protocol.fromPrefs(prefs)
        val selectedPreset: String
        selectedPreset = when (protocol) {
            Protocol.SSL -> getString(prefs, Key.SSL_PRESETS)
            Protocol.TCP -> getString(prefs, Key.TCP_PRESETS)
            Protocol.UDP -> getString(prefs, Key.UDP_PRESETS)
        }
        val preset = OutputPreset.fromString(selectedPreset)
        return protocol.toString() + if (preset == null) ": Unknown" else ": " + preset.alias
    }

    fun getPresetPrefKeyFromSharedPrefs(prefs: SharedPreferences): String {
        return when (Protocol.fromPrefs(prefs!!)) {
            Protocol.SSL -> Key.SSL_PRESETS
            Protocol.TCP -> Key.TCP_PRESETS
            Protocol.UDP -> Key.UDP_PRESETS
        }
    }
}
