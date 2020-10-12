package com.jon.common.repositories

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.roundToInt

class BatteryRepository private constructor() {
    private val lock = Any()
    private lateinit var batteryIntent: Intent

    fun getPercentage(): Int {
        synchronized(lock) {
            val level: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            return (level * 100 / scale.toFloat()).roundToInt()
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
