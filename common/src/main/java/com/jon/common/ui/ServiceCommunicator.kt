package com.jon.common.ui

interface ServiceCommunicator {
    fun startService()
    fun stopService()
    fun isServiceNull(): Boolean
    fun isServiceRunning(): Boolean
}