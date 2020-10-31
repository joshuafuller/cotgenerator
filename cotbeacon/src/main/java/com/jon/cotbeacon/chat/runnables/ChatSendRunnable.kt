package com.jon.cotbeacon.chat.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.utils.DataFormat
import com.jon.cotbeacon.chat.IChatRepository
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class ChatSendRunnable(
        prefs: SharedPreferences,
        private val chatRepository: IChatRepository,
        private val chatMessage: ChatCursorOnTarget
) : Runnable {

    private val dataFormat = DataFormat.fromPrefs(prefs)
    private val socket = DatagramSocket()

    override fun run() {
        try {
            Timber.i("Sending chat message: ${chatMessage.message}")
            val ip = InetAddress.getByName(ChatConstants.UDP_ALL_CHAT_ADDRESS)
            val buf = chatMessage.toBytes(dataFormat)
            socket.send(DatagramPacket(buf, buf.size, ip, ChatConstants.UDP_PORT))
            chatRepository.postChat(chatMessage)
        } catch (e: SocketException) {
            /* Thrown when the socket is closed externally whilst listening, i.e. when
             * the user tells the service to shutdown. No-op, just back out and finish the runnable */
        } catch (e: Exception) {
            Timber.e(e)
            chatRepository.postError(e.message ?: "Unknown exception")
        }
        Timber.i("Finishing ChatSendRunnable")
    }
}
