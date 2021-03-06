package com.jon.cotbeacon.service

import android.content.SharedPreferences
import com.jon.common.cot.CotRole
import com.jon.common.cot.CotTeam
import com.jon.common.cot.CursorOnTarget
import com.jon.common.cot.UtcTimestamp
import com.jon.common.di.IBuildResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getIntFromPair
import com.jon.common.prefs.getStringFromPair
import com.jon.common.repositories.IBatteryRepository
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.IGpsRepository
import com.jon.common.service.CotFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class BeaconCotFactory @Inject constructor(
        prefs: SharedPreferences,
        buildResources: IBuildResources,
        deviceUidRepository: IDeviceUidRepository,
        gpsRepository: IGpsRepository,
        batteryRepository: IBatteryRepository
) : CotFactory(prefs, buildResources, deviceUidRepository, gpsRepository, batteryRepository) {

    private val cot = CursorOnTarget(buildResources)

    override fun generate(): List<CursorOnTarget> {
        return if (cot.uid == null) initialise() else update()
    }

    override fun initialise(): List<CursorOnTarget> {
        cot.uid = deviceUidRepository.getUid()
        cot.callsign = prefs.getStringFromPair(CommonPrefs.CALLSIGN)
        cot.role = CotRole.fromPrefs(prefs)
        cot.team = CotTeam.fromPrefs(prefs)
        updateBattery()
        updateTime()
        updateGpsData()
        return listOf(cot)
    }

    override fun update(): List<CursorOnTarget> {
        updateBattery()
        updateTime()
        updateGpsData()
        return listOf(cot)
    }

    override fun clear() {
        cot.uid = null
    }

    private fun updateBattery() {
        cot.battery = batteryRepository.getPercentage()
    }

    private fun updateTime() {
        val now = UtcTimestamp.now()
        cot.time = now
        cot.start = now
        cot.setStaleDiff(
                dt = prefs.getIntFromPair(CommonPrefs.STALE_TIMER).toLong(),
                timeUnit = TimeUnit.MINUTES
        )
    }

    private fun updateGpsData() {
        cot.lat = gpsRepository.latitude()
        cot.lon = gpsRepository.longitude()
        cot.hae = gpsRepository.altitude()
        cot.course = gpsRepository.bearing()
        cot.speed = gpsRepository.speed()
        cot.ce = gpsRepository.circularError90()
        cot.le = gpsRepository.linearError90()
        val gpsSrc = getGpsSrc()
        cot.altsrc = gpsSrc
        cot.geosrc = gpsSrc
        updateDozeModeTags()
    }

    private fun getGpsSrc(): String {
        return when {
            gpsRepository.idleModeActive() -> "???"
            gpsRepository.hasGpsFix() -> "GPS"
            else -> "???"
        }
    }

    private fun updateDozeModeTags() {
        cot.setDozeModeTags(
                isDozeMode = gpsRepository.idleModeActive(),
                lastGpsUpdateMs = gpsRepository.getLastUpdateTime()
        )
    }
}
