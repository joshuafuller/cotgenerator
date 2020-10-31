package com.jon.cotbeacon

import android.content.Context
import com.jon.common.di.BuildResources
import java.text.SimpleDateFormat
import java.util.*

class BeaconBuildResources(context: Context) : BuildResources {
    override val appName = context.getString(R.string.app_name)
    override val platform = context.getString(R.string.app_name_all_caps)
    override val buildTimestamp: String = SimpleDateFormat("HH:mm:ss dd MMM yyyy z", Locale.ENGLISH).format(BuildConfig.BUILD_TIME)
    override val appId = BuildConfig.APPLICATION_ID
    override val versionName = BuildConfig.VERSION_NAME
    override val isDebug = BuildConfig.DEBUG
    override val serviceClass = BeaconCotService::class.java
}
