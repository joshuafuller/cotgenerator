package com.jon.cotbeacon.service.chat.runnables

import android.content.SharedPreferences
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.IThreadErrorListener
import com.jon.common.utils.DataFormat
import com.jon.cotbeacon.repositories.IChatRepository

abstract class ChatSendRunnable(
        prefs: SharedPreferences,
        errorListener: IThreadErrorListener,
        socketRepository: ISocketRepository,
        chatRepository: IChatRepository,
        protected val chatMessage: ChatCursorOnTarget
) : ChatRunnable(prefs, errorListener, socketRepository, chatRepository) {

    protected var dataFormat = DataFormat.fromPrefs(prefs)
}
