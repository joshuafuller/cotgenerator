package com.jon.cotbeacon.service

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.service.CotService
import com.jon.common.utils.MinimumVersions.NOTIFICATION_CHANNELS
import com.jon.common.utils.VersionUtils
import com.jon.cotbeacon.BeaconApplication
import com.jon.cotbeacon.R
import com.jon.cotbeacon.repositories.IChatRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BeaconCotService : CotService() {
    @Inject
    lateinit var chatRepository: IChatRepository

    @Inject
    lateinit var deviceUidRepository: IDeviceUidRepository

    private val chatThreadManager by lazy {
        ChatThreadManager(
                prefs = prefs,
                errorListener = this,
                chatRepository = chatRepository,
                deviceUidRepository = deviceUidRepository,
                socketRepository = socketRepository,
        )
    }

    override fun onCreate() {
        super.onCreate()
        if (VersionUtils.isAtLeast(NOTIFICATION_CHANNELS)) {
            notificationGenerator.createNotificationChannel(R.string.chat_notification_title)
        }

        chatRepository.getLatestChat().observe(this) {
            if (!it.isSelf && !BeaconApplication.chatFragmentIsVisible) {
                /* Only show a notification if this isn't a self-message, and if the chat fragment
                * isn't already open. */
                notificationGenerator.showNotification(
                        iconId = R.drawable.chat,
                        title = "Message from ${it.callsign}",
                        subtitle = "${it.start.shortLocalTimestamp()}: ${it.message}"
                )
            }
        }
    }

    override fun start() {
        super.start()
        chatThreadManager.start()
    }

    override fun shutdown() {
        super.shutdown()
        chatThreadManager.shutdown()
    }

    fun sendChat(chatMessage: ChatCursorOnTarget) {
        chatThreadManager.sendChat(chatMessage)
    }
}