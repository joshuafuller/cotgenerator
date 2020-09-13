package com.jon.cotbeacon

import com.jon.common.CotApplication
import com.jon.common.variants.Variant

class BeaconApplication : CotApplication() {
    override fun onCreate() {
        super.onCreate()

        /* To supply app-specific info during runtime like package name, build time/version, etc. */
        Variant.setAppVariantRepository(BeaconRepo())
    }
}
