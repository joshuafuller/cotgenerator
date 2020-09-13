package com.jon.cotbeacon

import android.content.SharedPreferences
import com.jon.common.cot.CotRole
import com.jon.common.cot.CotTeam
import com.jon.common.cot.CursorOnTarget
import com.jon.common.cot.UtcTimestamp
import com.jon.common.service.CotFactory
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils
import java.util.concurrent.TimeUnit

internal class BeaconCotFactory(prefs: SharedPreferences) : CotFactory(prefs) {
    private val cot = CursorOnTarget()

    override fun generate(): List<CursorOnTarget> {
        return if (cot.uid == null) initialise() else update()
    }

    override fun initialise(): List<CursorOnTarget> {
        cot.uid = deviceUidRepository.getUid()
        cot.callsign = PrefUtils.getString(prefs, Key.CALLSIGN)
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
        cot.setStaleDiff(PrefUtils.getInt(prefs, Key.STALE_TIMER).toLong(), TimeUnit.MINUTES)
    }

    private fun updateGpsData() {
        cot.lat = gpsRepository.latitude()
        cot.lon = gpsRepository.longitude()
        cot.hae = gpsRepository.altitude()
        cot.course = gpsRepository.bearing()
        cot.speed = gpsRepository.speed()
        cot.ce = gpsRepository.circularError90()
        cot.le = gpsRepository.linearError90()
        cot.altsrc = gpsRepository.gpsSource()
        cot.geosrc = gpsRepository.gpsSource()
    }
}
