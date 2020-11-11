package com.jon.common.service

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.jon.common.repositories.IGpsRepository
import timber.log.Timber
import javax.inject.Inject

internal class GpsLocationCallback @Inject constructor(private val gpsRepository: IGpsRepository) : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        Timber.d("onLocationResult %s", locationResult.lastLocation)
        locationResult.lastLocation?.let { gpsRepository.setLocation(it) }
    }
}
