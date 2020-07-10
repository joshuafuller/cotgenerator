package com.jon.beacon;

import com.jon.common.service.CotService;

public class BeaconService extends CotService {
    @Override
    public void onCreate() {
        super.onCreate();

        /* Initialise the GPS request, independent of any preferences */
        initialiseFusedLocationClient();
    }
}
