package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.di.IBuildResources
import com.jon.common.repositories.IGpsRepository
import com.jon.common.repositories.IBatteryRepository
import com.jon.common.repositories.IDeviceUidRepository

abstract class CotFactory(
        protected val prefs: SharedPreferences,
        protected val buildResources: IBuildResources,
        protected val deviceUidRepository: IDeviceUidRepository,
        protected val gpsRepository: IGpsRepository,
        protected val batteryRepository: IBatteryRepository
) {
    abstract fun generate(): List<CursorOnTarget>
    protected abstract fun initialise(): List<CursorOnTarget>
    protected abstract fun update(): List<CursorOnTarget>
    abstract fun clear()
}
