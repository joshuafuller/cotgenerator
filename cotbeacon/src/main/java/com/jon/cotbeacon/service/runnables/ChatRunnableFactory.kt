package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.utils.Protocol
import com.jon.cotbeacon.repositories.IChatRepository

class ChatRunnableFactory(
        private val prefs: SharedPreferences,
        private val threadErrorListener: IThreadErrorListener,
        private val socketRepository: ISocketRepository,
        private val chatRepository: IChatRepository,
) {
    fun getListenRunnable(deviceUidRepository: IDeviceUidRepository): ChatListenRunnable {
        return when (Protocol.fromPrefs(prefs)) {
            Protocol.UDP -> UdpListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
            Protocol.TCP -> TcpListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
            Protocol.SSL -> SslListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
        }
    }

    fun getSendRunnable(chatMessage: ChatCursorOnTarget): ChatSendRunnable {
        return when (Protocol.fromPrefs(prefs)) {
            Protocol.UDP -> UdpSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
            Protocol.TCP -> TcpSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
            Protocol.SSL -> SslSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
        }
    }
}
