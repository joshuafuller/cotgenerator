package com.jon.cot.generator;

import com.jon.cot.common.AppSpecific;
import com.jon.cot.common.CotApplication;

public class GeneratorApplication extends CotApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        /* To supply app-specific info during runtime like package name, build time/version, etc. */
        AppSpecific.setReferenceRepo(new GeneratorRepo());
    }
}
