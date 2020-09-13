package com.jon.common.repositories

import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BatteryRepository private constructor() {
    private val lock = Any()
    private lateinit var batteryIntent: Intent
    private var batteryPercentage: Int = 100

    fun setPercentage(percentage: Int) {
        synchronized(lock) {
            batteryPercentage = percentage
        }
    }

    fun getPercentage(): Int {
        synchronized(lock) {
            return batteryPercentage
        }
    }

    fun initialise(context: Context) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryIntent = context.registerReceiver(null, intentFilter)!!
    }

    companion object {
        private val instance = BatteryRepository()
        fun getInstance() = instance
    }
}
