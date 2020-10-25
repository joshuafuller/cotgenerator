package com.jon.common.variants

import android.content.SharedPreferences
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.XmlRes
import com.jon.common.presets.OutputPreset

object Variant {
    private var injector: VariantInjector = DefaultInjector()

    fun setInjector(variantInjector: VariantInjector) {
        injector = variantInjector
    }

    fun getCotFactory(prefs: SharedPreferences) = injector.getCotFactory(prefs)
    fun getSettingsFragment() = injector.getSettingsFragment()
    fun getBuildDate() = injector.buildDate
    fun getAppId() = injector.appId
    fun getAppName() = injector.appName
    fun getPermissionRationale() = injector.permissionRationale
    fun getVersionName() = injector.versionName
    fun getPlatform() = injector.platform
    fun isDebug() = injector.isDebug
    @XmlRes fun getSettingsXmlId() = injector.settingsXmlId
    @ColorRes fun getIconColourId() = injector.iconColourId
    @ColorRes fun getAccentColourId() = injector.accentColourId
    @LayoutRes fun getMainActivityLayoutId() = injector.mainActivityLayoutId
    @IdRes fun getNavHostFragmentId() = injector.navHostFragmentId
    @IdRes fun getStartStopButtonId() = injector.startStopButtonId

    fun getMainToListDirections() = injector.mainToListDirections
    fun getMainToAboutDirections() = injector.mainToAboutDirections
    fun getListToEditDirections(preset: OutputPreset?) = injector.listToEditDirections(preset)
}
