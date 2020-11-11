package com.jon.cotbeacon.service.chat.runnables

import android.content.SharedPreferences
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.cotbeacon.repositories.IChatRepository
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.Socket

class TcpChatListenRunnable(
        prefs: SharedPreferences,
        errorListener: IThreadErrorListener,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        deviceUidRepository: IDeviceUidRepository,
) : ChatListenRunnable(prefs, errorListener, socketRepository, chatRepository, deviceUidRepository) {

    private var socket: Socket? = null
    private var inputStream: InputStream? = null

    override fun run() {
        safeInitialise {
            socket = socketRepository.getTcpSocket()
            inputStream = BufferedInputStream(socket?.getInputStream())
        } ?: return

        while (true) {
            postErrorIfThrowable {
                Timber.i("Listening for chat from port %d to %d", socket?.port, socket?.localPort)
                val bytes = ByteArray(PACKET_BUFFER_SIZE)
                val length = inputStream?.read(bytes) ?: return@postErrorIfThrowable
                if (length < 0) {
                    return@postErrorIfThrowable
                }
                val receivedBytes = bytes.copyOf(length)
                val xml = String(receivedBytes)
                if (!xml.contains("All Chat Rooms")) {
                    /* Ignore if it's a PLI packet, for example */
                    return@postErrorIfThrowable
                }
                val chat = ChatCursorOnTarget.fromBytes(receivedBytes)
                if (chat?.uid != deviceUid) {
                    /* Only process the packet if it's not a loopback message */
                    dealWithChat(chat)
                }
            } ?: break
        }
        Timber.i("Finishing TcpChatListenRunnable")
        close()
    }

    override fun close() {
        safeClose(socket)
        safeClose(inputStream)
        socketRepository.clearSockets()
    }
}
