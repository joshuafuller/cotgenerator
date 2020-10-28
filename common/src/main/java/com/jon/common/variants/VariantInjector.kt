package com.jon.common.variants

import android.content.SharedPreferences
import androidx.navigation.NavDirections
import com.jon.common.presets.OutputPreset
import com.jon.common.service.CotFactory
import com.jon.common.ui.main.SettingsFragment
import java.util.*

interface VariantInjector {
    val buildDate: Date
    val appId: String
    val appName: String
    val versionName: String
    val platform: String
    val isDebug: Boolean
    val accentColourId: Int
    val startStopButtonId: Int

    val mainToListDirections: NavDirections

    fun getSettingsFragment(): SettingsFragment
    fun getCotFactory(prefs: SharedPreferences): CotFactory
    fun listToEditDirections(preset: OutputPreset?): NavDirections
}
