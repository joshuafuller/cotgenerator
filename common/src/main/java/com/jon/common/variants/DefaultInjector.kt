package com.jon.common.variants

import android.content.SharedPreferences
import androidx.navigation.NavDirections
import com.jon.common.presets.OutputPreset
import com.jon.common.service.CotFactory
import com.jon.common.variants.InjectorUtils.getBlankCotFactory
import com.jon.common.variants.InjectorUtils.getBlankNavDirections
import java.util.*

internal class DefaultInjector : VariantInjector {
    override val buildDate: Date = Date()
    override val appId: String = ""
    override val appName: String = ""
    override val permissionRationale: String = ""
    override val versionName: String = ""
    override val platform: String = ""
    override val isDebug: Boolean = true
    override val settingsXmlId: Int = 0
    override val iconColourId: Int = 0
    override val accentColourId: Int = 0
    override val mainActivityLayoutId: Int = 0
    override val navHostFragmentId: Int = 0
    override val startStopButtonId: Int = 0
    override val mainToListDirections: NavDirections = getBlankNavDirections()
    override val mainToAboutDirections: NavDirections = getBlankNavDirections()

    override fun getCotFactory(prefs: SharedPreferences): CotFactory = getBlankCotFactory(prefs)
    override fun listToEditDirections(preset: OutputPreset?): NavDirections = getBlankNavDirections()
}
