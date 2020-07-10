package com.jon.cot.common.service;

import android.location.Location;

import androidx.annotation.Nullable;

public class GpsCoords {
    private static final float ZERO = 0.0f;
    private static GpsCoords instance;

    synchronized public static GpsCoords getInstance() {
        if (instance == null) {
            instance = new GpsCoords();
        }
        return instance;
    }

    @Nullable
    private Location lastLocation = null;

    private GpsCoords() { /* blank */ }

    public void update(@Nullable Location location) {
        lastLocation = location;
    }

    public double latitude() {
        return lastLocation != null ? lastLocation.getLatitude() : ZERO;
    }

    public double longitude() {
        return lastLocation != null ? lastLocation.getLongitude() : ZERO;
    }
}
