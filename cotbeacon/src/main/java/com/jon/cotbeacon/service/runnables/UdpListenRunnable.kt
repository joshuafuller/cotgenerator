package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.cotbeacon.service.ChatConstants
import timber.log.Timber
import java.net.DatagramPacket
import java.net.MulticastSocket

class UdpListenRunnable(
        prefs: SharedPreferences,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        deviceUidRepository: IDeviceUidRepository,
) : ChatListenRunnable(prefs, socketRepository, chatRepository, deviceUidRepository) {

    private lateinit var socket: MulticastSocket

    override fun run() {
        safeInitialise {
            socket = socketRepository.getUdpInputSocket(
                    group = ChatConstants.UDP_ALL_CHAT_ADDRESS,
                    port = ChatConstants.UDP_PORT
            )
        } ?: return

        while (true) {
            postErrorIfThrowable {
                Timber.i("Listening for chat...")
                val bytes = ByteArray(PACKET_BUFFER_SIZE)
                val packet = DatagramPacket(bytes, PACKET_BUFFER_SIZE)
                socket.receive(packet)
                val receivedBytes = packet.data.copyOf(packet.length)
                val chat = ChatCursorOnTarget.fromBytes(receivedBytes)
                if (chat?.uid != deviceUid) {
                    /* Only process the packet if it's not a loopback message */
                    dealWithChat(chat)
                }
            } ?: break
        }
        Timber.i("Finishing UdpListenRunnable")
        close()
    }

    override fun close() {
        safeClose(socket)
    }
}
