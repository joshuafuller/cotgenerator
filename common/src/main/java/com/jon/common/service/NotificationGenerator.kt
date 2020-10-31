package com.jon.common.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.jon.common.R
import com.jon.common.di.IBuildResources
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.Protocol
import javax.inject.Inject

class NotificationGenerator @Inject constructor(
        private val context: Context,
        private val prefs: SharedPreferences,
        private val buildResources: IBuildResources
) : INotificationGenerator {

    private val stopServicePendingIntentId = GenerateInt.next()
    private val foregroundChannelId = "${buildResources.appId}.FOREGROUND"

    private lateinit var foregroundNotificationBuilder: NotificationCompat.Builder

    override fun getForegroundNotification(): Notification {
        /* Intent to stop the service when the notification button is tapped */
        val stopPendingIntent = PendingIntent.getService(
                context,
                stopServicePendingIntentId,
                Intent(context, buildResources.serviceClass).setAction(CotService.STOP_SERVICE),
                0
        )
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            foregroundChannelId
        } else {
            buildResources.appName
        }
        foregroundNotificationBuilder = NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(buildResources.appName)
                .setContentText(getPresetInfoString(prefs))
                .addAction(R.drawable.stop, context.getString(R.string.notification_stop), stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            foregroundNotificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            foregroundNotificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        }
        return foregroundNotificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
                NotificationChannel(
                        foregroundChannelId,
                        buildResources.appName,
                        NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    lightColor = Color.BLUE
                    lockscreenVisibility = Notification.VISIBILITY_SECRET
                    enableVibration(false)
                    setSound(null, null)
                    setShowBadge(false)
                }
        )
    }


    private fun getPresetInfoString(prefs: SharedPreferences): String {
        val protocol = Protocol.fromPrefs(prefs)
        val preset = OutputPreset.fromString(prefs.getStringFromPair(protocol.presetPref))
        return protocol.toString() + if (preset == null) ": Unknown" else ": " + preset.alias
    }

}