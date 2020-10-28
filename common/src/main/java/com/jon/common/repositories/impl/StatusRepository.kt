package com.jon.common.repositories.impl

import androidx.lifecycle.LiveData
import com.jon.common.repositories.IStatusRepository
import com.jon.common.service.ServiceState
import com.rugovit.eventlivedata.MutableEventLiveData
import javax.inject.Inject

class StatusRepository @Inject constructor() : IStatusRepository {
    private val lock = Any()
    private val currentStatus = MutableEventLiveData<ServiceState>()

    override fun getStatus(): LiveData<ServiceState> {
        synchronized(lock) {
            return currentStatus
        }
    }

    override fun setStatus(state: ServiceState) {
        synchronized(lock) {
            currentStatus.postValue(state)
        }
    }
}
