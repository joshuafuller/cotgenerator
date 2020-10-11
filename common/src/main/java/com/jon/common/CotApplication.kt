package com.jon.common

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.jon.common.repositories.BatteryRepository
import timber.log.Timber
import timber.log.Timber.DebugTree

open class CotApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        /* Set night mode */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        /* Initialise logging */
        Timber.plant(object : DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return "(" + element.fileName + ":" + element.lineNumber + ")"
            }
        })

        /* Register a sticky intent to allow fetching battery status during runtime */
        BatteryRepository.getInstance().initialise(context)
    }

    companion object {
        private var instance: CotApplication? = null
        val context: Context
            get() = instance!!.applicationContext

        var activityIsVisible = false
    }
}