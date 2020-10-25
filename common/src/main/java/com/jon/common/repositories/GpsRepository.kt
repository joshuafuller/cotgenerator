package com.jon.common.repositories

import android.location.Location
import android.os.Build
import timber.log.Timber

class GpsRepository private constructor() {
    private val lock = Any()
    private var lastLocation: Location? = null

    fun setLocation(location: Location?) {
        synchronized(lock) {
            if (location != null) {
                Timber.d("Updating GPS to %f %f", location.latitude, location.longitude)
            } else {
                Timber.d("Null location")
            }
            lastLocation = location
        }
    }

    fun latitude() = lastLocation?.latitude ?: ZERO

    fun longitude() = lastLocation?.longitude ?: ZERO

    fun altitude() = lastLocation?.altitude ?: ZERO

    fun bearing(): Double {
        return if (lastLocation?.hasBearing() == true) {
            lastLocation?.bearing?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    fun speed(): Double {
        return if (lastLocation?.hasSpeed() == true) {
            lastLocation?.speed?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    fun circularError90(): Double {
        return if (lastLocation?.hasAccuracy() == true) {
            lastLocation?.accuracy?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    fun linearError90(): Double {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && lastLocation?.hasVerticalAccuracy() == true) {
            lastLocation?.verticalAccuracyMeters?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    fun gpsSource(): String {
        return if (lastLocation == null) "GPS" else "NO-GPS-FIX"
    }

    companion object {
        private val instance = GpsRepository()
        fun getInstance() = instance

        private const val UNKNOWN: Double = 99999999.0
        private const val ZERO: Double = 0.0
    }
}
