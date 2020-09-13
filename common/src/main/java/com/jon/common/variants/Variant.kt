package com.jon.common.variants

import android.content.SharedPreferences
import androidx.annotation.ColorRes
import androidx.annotation.MenuRes
import androidx.annotation.XmlRes

object Variant {
    private lateinit var repo: VariantRepo /* = DefaultVariantRepo()*/

    fun setAppVariantRepository(variantRepo: VariantRepo) {
        repo = variantRepo
    }

    fun getSettingsFragment() = repo.mainFragment
    fun getCotFactory(prefs: SharedPreferences) = repo.getCotFactory(prefs)
    fun getBuildDate() = repo.buildDate
    fun getBuildVersionCode() = repo.buildVersionCode
    fun getAppId() = repo.appId
    fun getAppName() = repo.appName
    fun getPermissionRationale() = repo.permissionRationale
    fun getVersionName() = repo.versionName
    fun getPlatform() = repo.platform
    fun isDebug() = repo.isDebug
    fun getCotServiceClass() = repo.cotServiceClass
    fun getListActivityClass() = repo.listActivityClass
    @XmlRes fun getSettingsXmlId() = repo.settingsXmlId
    @ColorRes fun getIconColourId() = repo.iconColourId
}
