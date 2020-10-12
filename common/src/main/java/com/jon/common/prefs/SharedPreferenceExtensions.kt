package com.jon.common.prefs

import android.content.SharedPreferences

/* Some QoL utility methods for pulling values/defaults from the SharedPreferences */
fun SharedPreferences.getStringFromPair(pref: PrefPair<String>): String {
    return this.getString(pref.key, pref.default)!!
}

fun SharedPreferences.getBooleanFromPair(pref: PrefPair<Boolean>): Boolean {
    return this.getBoolean(pref.key, pref.default)
}

fun SharedPreferences.getIntFromPair(pref: PrefPair<Int>): Int {
    return this.getInt(pref.key, pref.default)
}

fun SharedPreferences.parseIntFromPair(pref: PrefPair<String>): Int {
    return this.getString(pref.key, pref.default)!!.toInt()
}

fun SharedPreferences.parseDoubleFromPair(pref: PrefPair<String>): Double {
    return this.getString(pref.key, pref.default)!!.toDouble()
}
