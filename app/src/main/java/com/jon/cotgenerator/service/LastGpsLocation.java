package com.jon.cotgenerator.service;

import android.location.Location;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.EmergencyCursorOnTarget;
import com.jon.cotgenerator.cot.PliCursorOnTarget;

class LastGpsLocation {
    private static final String TAG = LastGpsLocation.class.getSimpleName();
    private static final float UNKNOWN = 99999999.0f;
    private static final float ZERO = 0.0f;
    private static Location lastLocation = null;

    static void update(Location location) {
        lastLocation = location;
    }

    static void updateCot(CursorOnTarget cot) {
        if (lastLocation == null) {
            cot.ce = UNKNOWN;
            cot.le = UNKNOWN;
        } else {
            cot.lat = lastLocation.getLatitude();
            cot.lon = lastLocation.getLongitude();
            cot.hae = lastLocation.hasAltitude() ? lastLocation.getAltitude() : ZERO;
            cot.ce = lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : UNKNOWN;
            cot.le = lastLocation.hasVerticalAccuracy() ? lastLocation.getVerticalAccuracyMeters() : UNKNOWN;
        }

    }

    static void updatePli(PliCursorOnTarget cot) {
        updateCot(cot);
        if (lastLocation == null) {
            cot.altsrc = "???";
            cot.geosrc = "???";
            cot.course = ZERO;
            cot.speed = ZERO;
        } else {
            cot.altsrc = "GPS";
            cot.geosrc = "GPS";
            cot.course = lastLocation.hasBearing() ? lastLocation.getBearing() : ZERO;
            cot.speed = lastLocation.hasSpeed() ? lastLocation.getSpeed() : ZERO;
        }
    }
}
