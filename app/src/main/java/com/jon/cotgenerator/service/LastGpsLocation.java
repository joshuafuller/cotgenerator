package com.jon.cotgenerator.service;

import android.location.Location;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;

class LastGpsLocation {
    private static final String TAG = LastGpsLocation.class.getSimpleName();
    private static final float UNKNOWN = 99999999.0f;
    private static final float ZERO = 0.0f;
    private static Location lastLocation = null;

    static void update(Location location) {
        Log.i(TAG, "Received GPS update: " + location);
        lastLocation = location;
    }

    static void updateCot(CursorOnTarget cot) {
        if (lastLocation == null) {
//            cot.lat = ZERO;
//            cot.lon = ZERO;
//            cot.hae = ZERO;
            cot.ce = UNKNOWN;
            cot.le = UNKNOWN;
            cot.course = ZERO;
            cot.speed = ZERO;
            cot.altsrc = "???";
            cot.geosrc = "???";
        } else {
            cot.lat = lastLocation.getLatitude();
            cot.lon = lastLocation.getLongitude();
            cot.hae = lastLocation.hasAltitude() ? lastLocation.getAltitude() : ZERO;
            cot.ce = lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : UNKNOWN;
            cot.le = lastLocation.hasVerticalAccuracy() ? lastLocation.getVerticalAccuracyMeters() : UNKNOWN;
            cot.course = lastLocation.hasBearing() ? lastLocation.getBearing() : ZERO;
            cot.speed = lastLocation.hasSpeed() ? lastLocation.getSpeed() : ZERO;
            cot.altsrc = "GPS";
            cot.geosrc = "GPS";
        }
    }
}
