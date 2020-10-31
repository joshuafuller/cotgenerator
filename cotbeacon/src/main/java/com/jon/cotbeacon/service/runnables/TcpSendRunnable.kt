package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.ISocketRepository
import com.jon.common.utils.DataFormat
import com.jon.cotbeacon.repositories.IChatRepository
import timber.log.Timber
import java.io.OutputStream
import java.net.Socket

class TcpSendRunnable(
        prefs: SharedPreferences,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        chatMessage: ChatCursorOnTarget,
) : ChatSendRunnable(prefs, socketRepository, chatRepository, chatMessage) {

    private lateinit var socket: Socket
    private var outputStream: OutputStream? = null

    override fun run() {
        safeInitialise {
            socket = socketRepository.getTcpSocket()
            outputStream = socketRepository.getOutputStream(socket)
        } ?: return

        postErrorIfThrowable {
            Timber.i("Sending chat message: ${chatMessage.message} to port %d from %d", socket.port, socket.localPort)
            outputStream?.let {
                it.write(chatMessage.toBytes(DataFormat.XML))
                it.flush()
                chatRepository.postChat(chatMessage)
            }
        }

        Timber.i("Finishing TcpSendRunnable")
    }
}
