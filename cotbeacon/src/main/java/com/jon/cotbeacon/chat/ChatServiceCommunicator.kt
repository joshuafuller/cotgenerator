package com.jon.cotbeacon.chat

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.ui.ServiceCommunicator

interface ChatServiceCommunicator : ServiceCommunicator {
    fun sendChat(chat: ChatCursorOnTarget)
}
