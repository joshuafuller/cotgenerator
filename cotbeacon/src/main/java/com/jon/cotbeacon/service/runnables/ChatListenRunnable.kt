package com.jon.cotbeacon.service.runnables

import android.content.SharedPreferences
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.cot.CotTeam
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.cotbeacon.repositories.IChatRepository
import timber.log.Timber
import java.io.Closeable
import kotlin.math.abs

abstract class ChatListenRunnable(
        prefs: SharedPreferences,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        protected val deviceUidRepository: IDeviceUidRepository,
) : ChatRunnable(prefs, socketRepository, chatRepository), Closeable {

    protected val deviceUid = deviceUidRepository.getUid()

    /* Only difference to the superclass is the close() call in the catch block */
    override fun safeInitialise(initialisation: () -> Unit): Any? {
        return try {
            initialisation()
            Any()
        } catch (t: Throwable) {
            Timber.e(t)
            close()
            chatRepository.postError(t.message ?: "Unknown exception")
            null
        }
    }

    protected fun safeClose(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (t: Throwable) {
            /* No-op */
        }
    }

    protected fun dealWithChat(chat: ChatCursorOnTarget?) {
        if (chat != null) {
            chat.team = getCotTeam(chat.uid)
            chatRepository.postChat(chat)
            Timber.i("Received valid chat from ${chat.callsign}: ${chat.message}")
        }
    }

    private fun getCotTeam(uid: String?): CotTeam {
        return if (uid == null) {
            CotTeam.WHITE
        } else {
            val index = abs(uid.hashCode()) % CotTeam.values().size
            CotTeam.values()[index]
        }
    }

    protected companion object {
        const val PACKET_BUFFER_SIZE = 2048 // bytes
    }
}