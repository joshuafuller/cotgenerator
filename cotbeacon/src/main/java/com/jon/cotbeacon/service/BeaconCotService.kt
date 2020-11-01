package com.jon.cotbeacon.service

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.service.CotService
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