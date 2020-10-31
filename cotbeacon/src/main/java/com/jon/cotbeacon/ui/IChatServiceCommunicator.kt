package com.jon.cotbeacon.ui

import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.ui.IServiceCommunicator

interface IChatServiceCommunicator : IServiceCommunicator {
    fun sendChat(chat: ChatCursorOnTarget)
}
