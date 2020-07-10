package com.jon.beacon;

import com.jon.common.AppSpecific;
import com.jon.common.CotApplication;

public class BeaconApplication extends CotApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        /* To supply app-specific info during runtime like package name, build time/version, etc. */
        AppSpecific.setReferenceRepo(new BeaconRepo());
    }
}
