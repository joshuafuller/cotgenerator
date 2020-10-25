package com.jon.common.variants

import android.content.SharedPreferences
import androidx.navigation.NavDirections
import com.jon.common.presets.OutputPreset
import com.jon.common.service.CotFactory
import java.util.*

interface VariantInjector {
    val buildDate: Date
    val appId: String
    val appName: String
    val permissionRationale: String
    val versionName: String
    val platform: String
    val isDebug: Boolean
    val settingsXmlId: Int
    val iconColourId: Int
    val accentColourId: Int
    val mainActivityLayoutId: Int
    val navHostFragmentId: Int
    val startStopButtonId: Int

    val mainToListDirections: NavDirections
    val mainToAboutDirections: NavDirections

    fun getCotFactory(prefs: SharedPreferences): CotFactory
    fun listToEditDirections(preset: OutputPreset?): NavDirections
}
