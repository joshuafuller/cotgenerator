package com.jon.common.repositories.impl

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jon.common.repositories.IGpsRepository
import com.jon.common.utils.VersionUtils
import timber.log.Timber
import javax.inject.Inject

class GpsRepository @Inject constructor() : IGpsRepository {
    private val lock = Any()
    private val lastLocation = MutableLiveData<Location?>().also { it.value = null }

    override fun setLocation(location: Location) {
        synchronized(lock) {
            Timber.d("Updating GPS to %f %f", location.latitude, location.longitude)
            lastLocation.value = location
        }
    }

    override fun getLocation(): LiveData<Location?> {
        return lastLocation
    }

    override fun latitude() = lastLocation.value?.latitude ?: ZERO

    override fun longitude() = lastLocation.value?.longitude ?: ZERO

    override fun altitude() = lastLocation.value?.altitude ?: ZERO

    override fun bearing(): Double {
        return if (lastLocation.value?.hasBearing() == true) {
            lastLocation.value?.bearing?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    override fun speed(): Double {
        return if (lastLocation.value?.hasSpeed() == true) {
            lastLocation.value?.speed?.toDouble() ?: ZERO
        } else {
            ZERO
        }
    }

    override fun circularError90(): Double {
        return if (lastLocation.value?.hasAccuracy() == true) {
            lastLocation.value?.accuracy?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    override fun linearError90(): Double {
        return if (VersionUtils.isAtLeast(26) && lastLocation.value?.hasVerticalAccuracy() == true) {
            lastLocation.value?.verticalAccuracyMeters?.toDouble() ?: UNKNOWN
        } else {
            UNKNOWN
        }
    }

    override fun hasGpsFix(): Boolean {
        return lastLocation.value != null
    }

    companion object {
        private const val UNKNOWN: Double = 99999999.0
        private const val ZERO: Double = 0.0
    }
}
