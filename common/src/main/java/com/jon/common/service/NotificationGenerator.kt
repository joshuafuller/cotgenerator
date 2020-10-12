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
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant

class NotificationGenerator(
        private val context: Context,
        private val prefs: SharedPreferences
) {
    private lateinit var foregroundNotificationBuilder: NotificationCompat.Builder

    fun getForegroundNotification(): Notification {
        /* Intent to stop the service when the notification button is tapped */
        val stopPendingIntent = PendingIntent.getService(
                context,
                STOP_SERVICE_PENDING_INTENT,
                Intent(context, CotService::class.java).setAction(CotService.STOP_SERVICE),
                0
        )
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            FOREGROUND_CHANNEL_ID
        } else {
            FOREGROUND_CHANNEL_NAME
        }
        foregroundNotificationBuilder = NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(Variant.getAppName())
                .setContentText(getPresetInfoString(prefs))
                .addAction(R.drawable.stop, context.getString(R.string.menu_stop), stopPendingIntent)

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
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_NAME,
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

    private companion object {
        val STOP_SERVICE_PENDING_INTENT = GenerateInt.next()
        val FOREGROUND_CHANNEL_ID = "${Variant.getAppId()}.FOREGROUND"
        val FOREGROUND_CHANNEL_NAME = Variant.getAppName()
    }
}