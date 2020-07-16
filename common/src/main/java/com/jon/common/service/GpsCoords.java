package com.jon.common.service;

import android.location.Location;
import android.os.Build;

import androidx.annotation.Nullable;

public class GpsCoords {
    private static final float UNKNOWN = 99999999.0f;
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
        return valid() ? lastLocation.getLatitude() : ZERO;
    }

    public double longitude() {
        return valid() ? lastLocation.getLongitude() : ZERO;
    }

    public double altitude() {
        return (valid() && lastLocation.hasAltitude()) ? lastLocation.getAltitude() : ZERO;
    }

    public double bearing() {
        return (valid() && lastLocation.hasBearing()) ? lastLocation.getBearing() : ZERO;
    }

    public double speed() {
        return (valid() && lastLocation.hasSpeed()) ? lastLocation.getSpeed() : ZERO;
    }

    public double circularError90() {
        return (valid() && lastLocation.hasAccuracy()) ? lastLocation.getAccuracy() : UNKNOWN;
    }

    public double linearError90() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && valid() && lastLocation.hasVerticalAccuracy()) {
            return lastLocation.getVerticalAccuracyMeters();
        } else {
            return UNKNOWN;
        }
    }

    public String gpsSource() {
        return valid() ? "GPS" : "NO-GPS-FIX";
    }

    private boolean valid() {
        return lastLocation != null;
    }
}
