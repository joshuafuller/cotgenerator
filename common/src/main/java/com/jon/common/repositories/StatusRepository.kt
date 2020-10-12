package com.jon.common.repositories

import androidx.lifecycle.LiveData
import com.jon.common.service.ServiceState
import com.rugovit.eventlivedata.MutableEventLiveData

class StatusRepository private constructor() {
    private val lock = Any()
    private val currentStatus = MutableEventLiveData<ServiceState>()

    fun getStatus(): LiveData<ServiceState> {
        synchronized(lock) {
            return currentStatus
        }
    }

    fun setStatus(state: ServiceState) {
        synchronized(lock) {
            currentStatus.postValue(state)
        }
    }

    companion object {
        private val instance = StatusRepository()
        fun getInstance() = instance
    }
}