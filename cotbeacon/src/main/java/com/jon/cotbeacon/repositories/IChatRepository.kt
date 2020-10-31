package com.jon.cotbeacon.repositories

import androidx.lifecycle.LiveData
import com.jon.common.cot.ChatCursorOnTarget
import com.rugovit.eventlivedata.EventLiveData

interface IChatRepository {
    fun getChats(): LiveData<List<ChatCursorOnTarget>>
    fun postChat(chat: ChatCursorOnTarget)
    fun getErrors(): EventLiveData<String>
    fun postError(errorMessage: String)
}
