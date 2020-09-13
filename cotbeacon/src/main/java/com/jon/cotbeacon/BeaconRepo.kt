package com.jon.cotbeacon

import android.content.SharedPreferences
import com.jon.common.CotApplication
import com.jon.common.service.CotService
import com.jon.common.ui.listpresets.ListPresetsActivity
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragment
import com.jon.common.variants.VariantRepo
import java.util.*

class BeaconRepo : VariantRepo {
    override val mainFragment: MainFragment = BeaconFragment.getInstance()
    override val buildDate: Date = BuildConfig.BUILD_TIME
    override val buildVersionCode = BuildConfig.VERSION_CODE
    override val appId = BuildConfig.APPLICATION_ID
    override val appName = CotApplication.context.getString(R.string.app_name)
    override val permissionRationale = CotApplication.context.getString(R.string.permissionRationale)
    override val versionName = BuildConfig.VERSION_NAME
    override val platform = CotApplication.context.getString(R.string.appNameAllCaps)
    override val isDebug = BuildConfig.DEBUG
    override val cotServiceClass: Class<out CotService> = BeaconService::class.java
    override val mainActivityClass: Class<out MainActivity> = MainActivity::class.java
    override val listActivityClass: Class<out ListPresetsActivity> = BeaconListPresetsActivity::class.java
    override val settingsXmlId = R.xml.settings
    override val iconColourId = R.color.black
    override val menuId = R.menu.beacon_menu

    override fun getCotFactory(prefs: SharedPreferences) = BeaconCotFactory(prefs)
}
