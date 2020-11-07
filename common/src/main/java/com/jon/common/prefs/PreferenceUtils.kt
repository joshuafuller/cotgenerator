package com.jon.common.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import com.jon.common.R

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

/* Copied from androidx.core.content.res.TypedArrayUtils, since that method is package-private */
internal fun getDefaultAttr(context: Context): Int {
    val value = TypedValue()
    val attr = R.attr.editTextPreferenceStyle
    context.theme.resolveAttribute(attr, value, true)
    return if (value.resourceId != 0) {
        attr
    } else {
        android.R.attr.editTextPreferenceStyle
    }
}