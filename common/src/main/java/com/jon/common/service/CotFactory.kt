package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.repositories.BatteryRepository
import com.jon.common.repositories.GpsRepository

abstract class CotFactory(protected val prefs: SharedPreferences) {
    protected val gpsRepository = GpsRepository.getInstance()
    protected val batteryRepository = BatteryRepository.getInstance()

    abstract fun generate(): List<CursorOnTarget>
    protected abstract fun initialise(): List<CursorOnTarget>
    protected abstract fun update(): List<CursorOnTarget>
    abstract fun clear()
}
