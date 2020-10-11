package com.jon.cotgenerator

import com.jon.common.CotApplication
import com.jon.common.variants.Variant

@Suppress("unused")
class GeneratorApplication : CotApplication() {
    override fun onCreate() {
        super.onCreate()

        /* To supply app-specific info during runtime like package name, build time/version, etc. */
        Variant.setInjector(GeneratorInjector())
    }
}
