package com.jon.cotgenerator.service;

import android.location.Location;

import androidx.annotation.Nullable;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.PliCursorOnTarget;

class LastGpsLocation {
    private static final float UNKNOWN = 99999999.0f;
    private static final float ZERO = 0.0f;
    @Nullable
    private static Location lastLocation = null;

    static void update(@Nullable Location location) {
        lastLocation = location;
    }

    static void updateCot(CursorOnTarget cot) {
        if (hasFix()) {
            cot.lat = lastLocation.getLatitude();
            cot.lon = lastLocation.getLongitude();
            cot.hae = lastLocation.hasAltitude() ? lastLocation.getAltitude() : ZERO;
            cot.ce = lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : UNKNOWN;
            cot.le = lastLocation.hasVerticalAccuracy() ? lastLocation.getVerticalAccuracyMeters() : UNKNOWN;
        } else {
            cot.ce = UNKNOWN;
            cot.le = UNKNOWN;
        }
    }

    static void updatePli(PliCursorOnTarget cot) {
        updateCot(cot);
        if (hasFix()) {
            cot.altsrc = "GPS";
            cot.geosrc = "GPS";
            cot.course = lastLocation.hasBearing() ? lastLocation.getBearing() : ZERO;
            cot.speed = lastLocation.hasSpeed() ? lastLocation.getSpeed() : ZERO;
        } else {
            cot.altsrc = "???";
            cot.geosrc = "???";
            cot.course = ZERO;
            cot.speed = ZERO;
        }
    }

    private static boolean hasFix() {
        return lastLocation != null;
    }

    static double latitude() {
        return hasFix() ? lastLocation.getLatitude() : ZERO;
    }

    static double longitude() {
        return hasFix() ? lastLocation.getLongitude() : ZERO;
    }
}
