package com.jon.cotbeacon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.service.CotService
import com.jon.common.utils.Notify
import timber.log.Timber


class BeaconBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val launchFromBootEnabled = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_BOOT)
            if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
                val serviceIntent = Intent(context, CotService::class.java).apply {
                    action = CotService.START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
