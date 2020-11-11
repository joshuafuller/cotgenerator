package com.jon.common

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.jon.common.di.IBuildResources
import com.jon.common.logging.DebugTree
import com.jon.common.logging.FileLoggingTree
import com.jon.common.logging.LogUtils
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getBooleanFromPair
import timber.log.Timber
import javax.inject.Inject

open class CotApplication : MultiDexApplication() {
    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var buildResources: IBuildResources

    override fun onCreate() {
        super.onCreate()
        instance = this

        /* Set night mode */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        if (BuildConfig.DEBUG) {
            /* Debug builds only */
            Timber.plant(DebugTree())
            Timber.d("Planted debug tree")
        }
        if (prefs.getBooleanFromPair(CommonPrefs.LOG_TO_FILE)) {
            /* If the user has configured file logging */
            LogUtils.startFileLogging(buildResources)
            Timber.d("Planted file logging tree")
        }
    }

    companion object {
        private var instance: CotApplication? = null
        val context: Context
            get() = instance!!.applicationContext
    }
}