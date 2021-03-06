package com.jon.cotbeacon.repositories

import androidx.lifecycle.LiveData
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.rugovit.eventlivedata.EventLiveData

interface IChatRepository {
    fun getLatestChat(): LiveData<ChatCursorOnTarget>
    fun getChats(): LiveData<List<ChatCursorOnTarget>>
    fun postChat(chat: ChatCursorOnTarget)
    fun getErrors(): EventLiveData<String>
    fun postError(errorMessage: String)
}
