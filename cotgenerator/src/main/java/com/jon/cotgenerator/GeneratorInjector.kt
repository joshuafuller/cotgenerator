package com.jon.cotgenerator

import android.content.SharedPreferences
import com.jon.common.CotApplication
import com.jon.common.service.CotFactory
import com.jon.common.service.CotService
import com.jon.common.ui.listpresets.ListPresetsActivity
import com.jon.common.ui.main.MainFragment
import com.jon.common.variants.VariantInjector
import java.util.*

class GeneratorInjector : VariantInjector {
    override val mainFragment: MainFragment = GeneratorFragment()
    override val buildDate: Date = BuildConfig.BUILD_TIME
    override val buildVersionCode = BuildConfig.VERSION_CODE
    override val appId = BuildConfig.APPLICATION_ID
    override val appName = CotApplication.context.getString(R.string.app_name)
    override val permissionRationale = CotApplication.context.getString(R.string.permission_rationale)
    override val versionName = BuildConfig.VERSION_NAME
    override val platform = CotApplication.context.getString(R.string.app_name_all_caps)
    override val isDebug = BuildConfig.DEBUG
    override val listActivityClass: Class<out ListPresetsActivity> = ListPresetsActivity::class.java
    override val settingsXmlId = R.xml.settings
    override val iconColourId = R.color.white

    override fun getCotFactory(prefs: SharedPreferences): CotFactory = GeneratorCotFactory(prefs)
}
