package com.jon.common.variants

import android.content.SharedPreferences
import androidx.annotation.ColorRes
import androidx.annotation.XmlRes

object Variant {
    private var injector: VariantInjector = DefaultInjector()

    fun setInjector(variantInjector: VariantInjector) {
        injector = variantInjector
    }

    fun getMainFragment() = injector.mainFragment
    fun getCotFactory(prefs: SharedPreferences) = injector.getCotFactory(prefs)
    fun getBuildDate() = injector.buildDate
    fun getBuildVersionCode() = injector.buildVersionCode
    fun getAppId() = injector.appId
    fun getAppName() = injector.appName
    fun getPermissionRationale() = injector.permissionRationale
    fun getVersionName() = injector.versionName
    fun getPlatform() = injector.platform
    fun isDebug() = injector.isDebug
    fun getCotServiceClass() = injector.cotServiceClass
    fun getListActivityClass() = injector.listActivityClass
    @XmlRes fun getSettingsXmlId() = injector.settingsXmlId
    @ColorRes fun getIconColourId() = injector.iconColourId
}
