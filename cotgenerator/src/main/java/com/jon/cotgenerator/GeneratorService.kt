package com.jon.cotgenerator

import com.jon.common.service.CotService
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils

class GeneratorService : CotService() {
    override fun onCreate() {
        super.onCreate()

        /* Only initialise the GPS requests if the option is enabled in settings */
        if (PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION)) {
            initialiseFusedLocationClient()
        }
    }
}
