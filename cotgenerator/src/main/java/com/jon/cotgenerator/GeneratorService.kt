package com.jon.cotgenerator

import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.service.CotService

class GeneratorService : CotService() {
    override fun onCreate() {
        super.onCreate()

        /* Only initialise the GPS requests if the option is enabled in settings */
        if (prefs.getBooleanFromPair(GeneratorPrefs.FOLLOW_GPS_LOCATION)) {
            initialiseFusedLocationClient()
        }
    }
}
