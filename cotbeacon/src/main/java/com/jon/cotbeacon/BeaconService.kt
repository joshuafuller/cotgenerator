package com.jon.cotbeacon

import com.jon.common.service.CotService

class BeaconService : CotService() {
    override fun onCreate() {
        super.onCreate()

        /* Initialise the GPS request, independent of any preferences */
        initialiseFusedLocationClient()
    }
}
