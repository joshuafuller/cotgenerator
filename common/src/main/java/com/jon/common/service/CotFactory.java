package com.jon.common.service;

import android.content.SharedPreferences;

import com.jon.common.cot.CursorOnTarget;
import com.jon.common.utils.Battery;

import java.util.List;

public abstract class CotFactory {
    protected final SharedPreferences prefs;
    protected final GpsCoords gpsCoords = GpsCoords.getInstance();
    protected final Battery battery = Battery.getInstance();

    protected CotFactory(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    protected abstract List<CursorOnTarget> generate();
    protected abstract List<CursorOnTarget> initialise();
    protected abstract List<CursorOnTarget> update();

    protected abstract void clear();
}
