package com.jon.common.variants

import android.content.SharedPreferences
import com.jon.common.service.CotFactory
import com.jon.common.service.CotService
import com.jon.common.ui.listpresets.ListPresetsActivity
import com.jon.common.ui.main.MainFragment
import java.util.*

interface VariantInjector {
    val mainFragment: MainFragment
    val buildDate: Date
    val buildVersionCode: Int
    val appId: String
    val appName: String
    val permissionRationale: String
    val versionName: String
    val platform: String
    val isDebug: Boolean
    val listActivityClass: Class<out ListPresetsActivity>
    val settingsXmlId: Int
    val iconColourId: Int

    fun getCotFactory(prefs: SharedPreferences): CotFactory
}
