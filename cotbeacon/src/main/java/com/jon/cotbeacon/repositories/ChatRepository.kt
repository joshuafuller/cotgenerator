package com.jon.cotbeacon.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jon.common.cot.ChatCursorOnTarget
import com.rugovit.eventlivedata.EventLiveData
import com.rugovit.eventlivedata.MutableEventLiveData
import javax.inject.Inject

class ChatRepository @Inject constructor() : IChatRepository {
    private val chats = ArrayList<ChatCursorOnTarget>()
    private val chatLiveData = MutableLiveData<List<ChatCursorOnTarget>>().also { it.value = chats }

    private val chatErrorEvent = MutableEventLiveData<String>()

    override fun getChats(): LiveData<List<ChatCursorOnTarget>> {
        return chatLiveData
    }

    override fun postChat(chat: ChatCursorOnTarget) {
        chats.add(chat)
        chatLiveData.postValue(chats)
    }

    override fun getErrors(): EventLiveData<String> {
        return chatErrorEvent
    }

    override fun postError(errorMessage: String) {
        chatErrorEvent.postValue(errorMessage)
    }
}
