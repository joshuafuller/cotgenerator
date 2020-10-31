package com.jon.cotbeacon

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.service.CotService
import com.jon.cotbeacon.chat.runnables.ChatListenRunnable
import com.jon.cotbeacon.chat.runnables.ChatSendRunnable
import com.jon.cotbeacon.chat.IChatRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class BeaconCotService : CotService() {
    @Inject
    lateinit var chatRepository: IChatRepository

    @Inject
    lateinit var deviceUiRepository: IDeviceUidRepository

    private var chatListeningExecutor = Executors.newSingleThreadExecutor()
    private var chatSendingExecutor = Executors.newSingleThreadExecutor()
    private lateinit var chatListenRunnable: ChatListenRunnable
    private lateinit var chatSendRunnable: ChatSendRunnable

    override fun start() {
        super.start()
        if (chatListeningExecutor.isShutdown) {
            chatListeningExecutor = Executors.newSingleThreadExecutor()
        }
        chatListenRunnable = ChatListenRunnable(chatRepository, deviceUiRepository)
        chatListeningExecutor.execute(chatListenRunnable)
    }

    override fun shutdown() {
        super.shutdown()
        chatListenRunnable.close()
        chatListeningExecutor.shutdownNow()
        chatSendingExecutor.shutdownNow()
    }

    fun sendChat(chatMessage: ChatCursorOnTarget) {
        Timber.i("Sending!")
        if (chatSendingExecutor.isShutdown) {
            chatSendingExecutor = Executors.newSingleThreadExecutor()
        }
        chatSendRunnable = ChatSendRunnable(prefs, chatRepository, chatMessage)
        chatSendingExecutor.execute(chatSendRunnable)
    }
}