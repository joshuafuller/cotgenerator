package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.List;

abstract class CotGenerator {
    protected static final double DEG_TO_RAD = Math.PI / 180.0;
    protected static final double RAD_TO_DEG = 1 / DEG_TO_RAD;
    protected SharedPreferences prefs;

    protected CotGenerator(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    static CotGenerator getFromPrefs(SharedPreferences prefs) {
        String dataType = PrefUtils.getString(prefs, Key.TRANSMITTED_DATA);
        switch (dataType) {
            case "GPS Position":
                return new GpsCotGenerator(prefs);
            case "Generated Icons":
                return new FakeCotGenerator(prefs);
            default:
                throw new IllegalArgumentException("Unsupported CotGenerator: " + dataType);
        }
    }

    protected abstract List<CursorOnTarget> generate();
    protected abstract List<CursorOnTarget> initialise();
    protected abstract List<CursorOnTarget> update();

    protected abstract void clear();
}
