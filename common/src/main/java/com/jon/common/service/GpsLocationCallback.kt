package com.jon.common.service

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.jon.common.repositories.IGpsRepository

internal class GpsLocationCallback(private val gpsRepository: IGpsRepository) : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let { gpsRepository.setLocation(it) }
    }
}
