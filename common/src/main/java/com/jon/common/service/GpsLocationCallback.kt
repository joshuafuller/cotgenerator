package com.jon.common.service

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.jon.common.repositories.GpsRepository

internal class GpsLocationCallback(private val gpsRepository: GpsRepository) : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        gpsRepository.setLocation(locationResult.lastLocation)
    }
}