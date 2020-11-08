package com.jon.cotbeacon.service.chat

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.service.ThreadManager
import com.jon.cotbeacon.prefs.BeaconPrefs
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.cotbeacon.service.chat.runnables.ChatListenRunnable
import com.jon.cotbeacon.service.chat.runnables.ChatSendRunnable
import timber.log.Timber
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
    private var listenRunnable: ChatListenRunnable? = null
    private var sendRunnable: ChatSendRunnable? = null

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
        synchronized(lock) {
            if (listeningExecutor.isShutdown) {
                listeningExecutor = Executors.newSingleThreadExecutor()
            }
            listenRunnable = runnableFactory.getListenRunnable(deviceUidRepository)
            listeningExecutor.execute(listenRunnable)
        }
    }

    override fun shutdown() {
        Timber.i("shutting down")
        synchronized(lock) {
            listenRunnable?.close()
            listeningExecutor.shutdownNow()
            sendingExecutor.shutdownNow()
        }
    }

    override fun restart() {
        Timber.i("restarting")
        synchronized(lock) {
            shutdown()
            if (chatIsEnabled()) {
                start()
            }
        }
    }

    override fun isRunning(): Boolean {
        synchronized(lock) {
            return !listeningExecutor.isTerminated
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        synchronized(lock) {
            if (isRunning()) {
                /* If any preferences are changed during runtime, kill the threads and reload with the new settings */
                restart()
            } else if (chatIsEnabled()) {
                /* If chat was previously disabled (threads weren't running) and now it's enabled, start it up */
                start()
            }
        }
    }

    fun sendChat(chatMessage: ChatCursorOnTarget) {
        synchronized(lock) {
            if (sendingExecutor.isShutdown) {
                sendingExecutor = Executors.newSingleThreadExecutor()
            }
            sendRunnable = runnableFactory.getSendRunnable(chatMessage)
            sendingExecutor.execute(sendRunnable)
        }
    }

    override fun onThreadError(throwable: Throwable) {
        mainHandler.post {
            errorListener.onThreadError(throwable)
        }
    }

    private fun chatIsEnabled(): Boolean {
        return prefs.getBooleanFromPair(BeaconPrefs.ENABLE_CHAT)
    }
}