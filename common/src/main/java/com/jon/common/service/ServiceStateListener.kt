package com.jon.common.service

interface ServiceStateListener {
    fun onServiceStateChanged(state: ServiceState, throwable: Throwable? = null)
}
