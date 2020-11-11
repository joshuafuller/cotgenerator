package com.jon.cotbeacon.service.chat

import android.content.SharedPreferences
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.utils.Protocol
import com.jon.common.utils.exhaustive
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.cotbeacon.service.chat.runnables.*

internal class ChatRunnableFactory(
        private val prefs: SharedPreferences,
        private val threadErrorListener: IThreadErrorListener,
        private val socketRepository: ISocketRepository,
        private val chatRepository: IChatRepository,
) {
    fun getListenRunnable(deviceUidRepository: IDeviceUidRepository): ChatListenRunnable {
        return when (Protocol.fromPrefs(prefs)) {
            Protocol.UDP -> UdpChatListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
            Protocol.TCP -> TcpChatListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
            Protocol.SSL -> SslChatListenRunnable(prefs, threadErrorListener, socketRepository, chatRepository, deviceUidRepository)
        }.exhaustive
    }

    fun getSendRunnable(chatMessage: ChatCursorOnTarget): ChatSendRunnable {
        return when (Protocol.fromPrefs(prefs)) {
            Protocol.UDP -> UdpChatSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
            Protocol.TCP -> TcpChatSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
            Protocol.SSL -> SslChatSendRunnable(prefs, threadErrorListener, socketRepository, chatRepository, chatMessage)
        }.exhaustive
    }
}
