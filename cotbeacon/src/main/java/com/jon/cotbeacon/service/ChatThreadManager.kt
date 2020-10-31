package com.jon.cotbeacon.service

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.ThreadManager
import com.jon.cotbeacon.service.runnables.ChatListenRunnable
import com.jon.cotbeacon.service.runnables.ChatRunnableFactory
import com.jon.cotbeacon.service.runnables.ChatSendRunnable
import com.jon.cotbeacon.repositories.IChatRepository
import java.util.concurrent.Executors

class ChatThreadManager(
        prefs: SharedPreferences,
        chatRepository: IChatRepository,
        private val deviceUidRepository: IDeviceUidRepository,
        socketRepository: ISocketRepository
) : ThreadManager() {

    private var listeningExecutor = Executors.newSingleThreadExecutor()
    private var sendingExecutor = Executors.newSingleThreadExecutor()
    private lateinit var listenRunnable: ChatListenRunnable
    private lateinit var sendRunnable: ChatSendRunnable

    private val runnableFactory = ChatRunnableFactory(prefs, socketRepository, chatRepository)

    override fun start() {
        if (listeningExecutor.isShutdown) {
            listeningExecutor = Executors.newSingleThreadExecutor()
        }
        listenRunnable = runnableFactory.getListenRunnable(deviceUidRepository)
        listeningExecutor.execute(listenRunnable)
    }

    override fun shutdown() {
        listenRunnable.close()
        listeningExecutor.shutdownNow()
        sendingExecutor.shutdownNow()
    }

    override fun isRunning(): Boolean {
        return !listeningExecutor.isTerminated
    }

    fun sendChat(chatMessage: ChatCursorOnTarget) {
        if (sendingExecutor.isShutdown) {
            sendingExecutor = Executors.newSingleThreadExecutor()
        }
        sendRunnable = runnableFactory.getSendRunnable(chatMessage)
        sendingExecutor.execute(sendRunnable)
    }
}