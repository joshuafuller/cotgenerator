package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.ISocketRepository
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.cotbeacon.service.ChatConstants
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpSendRunnable(
        prefs: SharedPreferences,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        chatMessage: ChatCursorOnTarget,
) : ChatSendRunnable(prefs, socketRepository, chatRepository, chatMessage) {

    private lateinit var socket: DatagramSocket

    override fun run() {
        safeInitialise {
            socket = DatagramSocket()
        } ?: return

        postErrorIfThrowable {
            Timber.i("Sending chat message: ${chatMessage.message}")
            val ip = InetAddress.getByName(ChatConstants.UDP_ALL_CHAT_ADDRESS)
            val buf = chatMessage.toBytes(dataFormat)
            socket.send(DatagramPacket(buf, buf.size, ip, ChatConstants.UDP_PORT))
            chatRepository.postChat(chatMessage)
        }

        Timber.i("Finishing ChatSendRunnable")
    }
}
