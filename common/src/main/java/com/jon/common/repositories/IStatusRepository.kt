package com.jon.common.repositories

import androidx.lifecycle.LiveData
import com.jon.common.service.ServiceState

interface IStatusRepository {
    fun getStatus(): LiveData<ServiceState>
    fun postStatus(state: ServiceState)
}
