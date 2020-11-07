package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.utils.DataFormat
import com.jon.cotbeacon.repositories.IChatRepository
import timber.log.Timber
import java.io.OutputStream
import java.net.Socket

class SslSendRunnable(
        prefs: SharedPreferences,
        errorListener: IThreadErrorListener,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        chatMessage: ChatCursorOnTarget,
) : ChatSendRunnable(prefs, errorListener, socketRepository, chatRepository, chatMessage) {

    private lateinit var socket: Socket
    private var outputStream: OutputStream? = null

    override fun run() {
        safeInitialise {
            socket = socketRepository.getSslSocket()
            outputStream = socketRepository.getOutputStream(socket)
        } ?: return

        postErrorIfThrowable {
            Timber.i("Sending chat message: ${chatMessage.message} from ${socket.localPort} to ${socket.port}")
            outputStream?.let {
                it.write(chatMessage.toBytes(DataFormat.XML))
                chatRepository.postChat(chatMessage)
            }
        }

        Timber.i("Finishing TcpSendRunnable")
    }
}
