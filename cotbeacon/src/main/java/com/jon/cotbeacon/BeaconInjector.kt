package com.jon.cotbeacon

import android.content.SharedPreferences
import androidx.navigation.NavDirections
import com.jon.common.CotApplication
import com.jon.common.presets.OutputPreset
import com.jon.common.service.CotFactory
import com.jon.common.ui.listpresets.ListPresetsFragmentDirections
import com.jon.common.ui.main.SettingsFragment
import com.jon.common.variants.VariantInjector
import java.util.*

class BeaconInjector : VariantInjector {
    override val buildDate: Date = BuildConfig.BUILD_TIME
    override val appId = BuildConfig.APPLICATION_ID
    override val appName = CotApplication.context.getString(R.string.app_name)
    override val permissionRationale = CotApplication.context.getString(R.string.permission_rationale)
    override val versionName = BuildConfig.VERSION_NAME
    override val platform = CotApplication.context.getString(R.string.app_name_all_caps)
    override val isDebug = BuildConfig.DEBUG
    override val settingsXmlId = R.xml.settings
    override val iconColourId = R.color.black
    override val accentColourId = R.color.colorAccent
    override val mainActivityLayoutId = R.layout.beacon_activity
    override val navHostFragmentId = R.id.nav_host_fragment
    override val startStopButtonId = R.id.start_stop_button
    override val mainToListDirections = MainFragmentDirections.actionMainToListPresets()
    override val mainToAboutDirections = MainFragmentDirections.actionMainToAbout()

    override fun getSettingsFragment(): SettingsFragment = BeaconSettingsFragment()
    override fun getCotFactory(prefs: SharedPreferences): CotFactory = BeaconCotFactory(prefs)
    override fun listToEditDirections(preset: OutputPreset?): NavDirections = ListPresetsFragmentDirections.actionListPresetsToBeaconEditPreset(preset)
}
