package com.jon.cotbeacon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.jon.common.di.IBuildResources
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.service.CotService
import com.jon.common.utils.Notify
import com.jon.common.utils.VersionUtils
import com.jon.cotbeacon.prefs.BeaconPrefs
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BeaconBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var buildResources: IBuildResources

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val launchFromBootEnabled = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_BOOT)
            if (launchFromBootEnabled && intent?.action == Intent.ACTION_BOOT_COMPLETED) {
                val serviceIntent = Intent(context, buildResources.serviceClass).apply {
                    action = CotService.START_SERVICE
                }
                if (VersionUtils.isAtLeast(26)) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Notify.toast(context, "Started transmitting location via CoT Beacon!")
            }
        } catch (e: Exception) {
            Timber.e(e)
            Notify.toast(context, "Error when launching CoT Beacon on device boot!")
        }
    }
}
