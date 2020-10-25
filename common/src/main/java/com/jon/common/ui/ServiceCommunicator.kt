package com.jon.common.ui

internal interface ServiceCommunicator {
    fun startService()
    fun stopService()
    fun isServiceNull(): Boolean
    fun isServiceRunning(): Boolean
}