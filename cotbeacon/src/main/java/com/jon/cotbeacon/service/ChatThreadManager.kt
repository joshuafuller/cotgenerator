package com.jon.cotbeacon.service

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.service.ThreadManager
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.cotbeacon.service.runnables.ChatListenRunnable
import com.jon.cotbeacon.service.runnables.ChatRunnableFactory
import com.jon.cotbeacon.service.runnables.ChatSendRunnable
import java.util.concurrent.Executors

class ChatThreadManager(
        prefs: SharedPreferences,
        errorListener: IThreadErrorListener,
        chatRepository: IChatRepository,
        private val deviceUidRepository: IDeviceUidRepository,
        socketRepository: ISocketRepository,
) : ThreadManager(prefs, errorListener), IThreadErrorListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var listeningExecutor = Executors.newSingleThreadExecutor()
    private var sendingExecutor = Executors.newSingleThreadExecutor()
    private lateinit var listenRunnable: ChatListenRunnable
    private lateinit var sendRunnable: ChatSendRunnable

    private val runnableFactory = ChatRunnableFactory(
            prefs = prefs,
            threadErrorListener = this,
            socketRepository = socketRepository,
            chatRepository = chatRepository
    )

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

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

    override fun onThreadError(throwable: Throwable) {
        mainHandler.post {
            errorListener.onThreadError(throwable)
        }
    }
}