package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.enums.TransmittedData;

import java.util.List;

abstract class CotGenerator {
    protected SharedPreferences prefs;

    protected CotGenerator(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    static CotGenerator getFromPrefs(SharedPreferences prefs) {
        TransmittedData dataType = TransmittedData.fromPrefs(prefs);
        switch (dataType) {
            case GPS:
                return new GpsCotGenerator(prefs);
            case FAKE:
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
