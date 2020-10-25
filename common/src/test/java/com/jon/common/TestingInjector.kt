package com.jon.common

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.service.CotFactory
import com.jon.common.variants.VariantInjector
import java.util.*

/* App-specific repo used for unit testing, since we don't have an Application class to set this value */
class TestingInjector : VariantInjector {
    override val buildDate: Date = Date()
    override val appId: String = ""
    override val appName: String = ""
    override val permissionRationale: String = ""
    override val versionName: String = "VERSION-NAME"
    override val platform: String = "PLATFORM"
    override val isDebug: Boolean = true
    override val settingsXmlId: Int = 0
    override val iconColourId: Int = 0
    override val accentColourId: Int = 0
    override val mainActivityLayoutId = 0
    override val navHostFragmentId = 0

    override fun getCotFactory(prefs: SharedPreferences): CotFactory = object : CotFactory(prefs) {
        override fun generate(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun initialise(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun update(): MutableList<CursorOnTarget> { return mutableListOf() }
        override fun clear() { }
    }
}