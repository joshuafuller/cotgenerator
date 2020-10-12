package com.jon.common

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.service.CotFactory
import com.jon.common.service.CotService
import com.jon.common.ui.listpresets.ListPresetsActivity
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragment
import com.jon.common.variants.VariantInjector
import java.util.*

/* App-specific repo used for unit testing, since we don't have an Application class to set this value */
class TestingInjector : VariantInjector {
    override val mainFragment: MainFragment = object : MainFragment() {}
    override val buildDate: Date = Date()
    override val buildVersionCode: Int = 0
    override val appId: String = ""
    override val appName: String = ""
    override val permissionRationale: String = ""
    override val versionName: String = "VERSION-NAME"
    override val platform: String = "PLATFORM"
    override val isDebug: Boolean = true
    override val listActivityClass: Class<out ListPresetsActivity> = ListPresetsActivity::class.java
    override val settingsXmlId: Int = 0
    override val iconColourId: Int = 0

    override fun getCotFactory(prefs: SharedPreferences): CotFactory = object : CotFactory(prefs) {
        override fun generate(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun initialise(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun update(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun clear() { }
    }
}