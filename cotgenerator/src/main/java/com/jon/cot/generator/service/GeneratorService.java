package com.jon.cot.generator.service;

import com.jon.cot.common.service.CotService;
import com.jon.cot.common.utils.Key;
import com.jon.cot.common.utils.PrefUtils;

public class GeneratorService extends CotService {
    @Override
    public void onCreate() {
        super.onCreate();

        /* Only initialise the GPS requests if the option is enabled in settings */
        if (PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION)) {
            initialiseFusedLocationClient();
        }
    }
}
