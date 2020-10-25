package com.jon.common.repositories

import android.location.Location
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class GpsRepository private constructor() {
    private val lock = Any()
    private val lastLocation = MutableLiveData<Location?>().also { it.value = null }

    fun setLocation(location: Location?) {
        synchronized(lock) {
            if (location != null) {
                Timber.d("Updating GPS to %f %f", location.latitude, location.longitude)
            } else {
                Timber.d("Null location")
            }
            lastLocation.value = location
        }
    }

    fun getLocation(): LiveData<Location?> {
        return lastLocation
    }

    fun latitude() = lastLocation.value?.latitude ?: ZERO

    fun longitude() = lastLocation.value?.longitude ?: ZERO

    fun altitude() = lastLocation.value?.altitude ?: ZERO

    fun bearing(): Double {
        return if (lastLocation.value?.hasBearing() == true) {
            lastLocation.value?.bearing?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    fun speed(): Double {
        return if (lastLocation.value?.hasSpeed() == true) {
            lastLocation.value?.speed?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    fun circularError90(): Double {
        return if (lastLocation.value?.hasAccuracy() == true) {
            lastLocation.value?.accuracy?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    fun linearError90(): Double {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && lastLocation.value?.hasVerticalAccuracy() == true) {
            lastLocation.value?.verticalAccuracyMeters?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    fun hasGpsFix(): Boolean {
        return lastLocation.value != null
    }

    companion object {
        private val instance = GpsRepository()
        fun getInstance() = instance

        private const val UNKNOWN: Double = 99999999.0
        private const val ZERO: Double = 0.0
    }
}
