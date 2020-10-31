package com.jon.cotbeacon.chat.runnables

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.cot.CotTeam
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.cotbeacon.chat.IChatRepository
import timber.log.Timber
import java.io.Closeable
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketException
import java.util.*
import kotlin.math.abs

class ChatListenRunnable(
        private val chatRepository: IChatRepository,
        deviceUidRepository: IDeviceUidRepository
) : Runnable, Closeable {

    private val socket = MulticastSocket(ChatConstants.UDP_PORT)
    private val deviceUid = deviceUidRepository.getUid()

    override fun run() {
        socket.joinGroup(InetAddress.getByName(ChatConstants.UDP_ALL_CHAT_ADDRESS))
        socket.loopbackMode = true

        while (true) {
            try {
                Timber.i("Listening for chat...")
                val bytes = ByteArray(BUFFER_SIZE)
                val packet = DatagramPacket(bytes, BUFFER_SIZE)
                socket.receive(packet)
                val receivedBytes = Arrays.copyOf(packet.data, packet.length)
                val chat = ChatCursorOnTarget.fromBytes(receivedBytes)
                if (chat == null) {
                    Timber.i("Null chat from ${String(receivedBytes)}")
                    chatRepository.postError("Invalid packet in port ${ChatConstants.UDP_PORT}")
                } else if (chat.uid == deviceUid) {
                    /* If this is a loopback message, ignore it */
                    continue
                } else {
                    chat.team = getCotTeam(chat.uid)
                    chatRepository.postChat(chat)
                    Timber.i("Received chat from ${chat.callsign}: ${chat.message}")
                }
            } catch (e: SocketException) {
                /* Thrown when the socket is closed externally whilst listening, i.e. when
                 * the user tells the service to shutdown. No-op, just back out and finish the runnable */
                Timber.w(e)
                break
            } catch (e: Exception) {
                Timber.e(e)
                chatRepository.postError(e.message ?: "Unknown exception")
                break
            }
        }
        Timber.i("Finishing ChatListenRunnable")
        close()
    }

    private fun getCotTeam(uid: String?): CotTeam {
        return if (uid == null) {
            CotTeam.WHITE
        } else {
            val index = abs(uid.hashCode()) % CotTeam.values().size
            CotTeam.values()[index]
        }
    }

    override fun close() {
        socket.close()
    }

    private companion object {
        const val BUFFER_SIZE = 2048 // bytes
    }
}
