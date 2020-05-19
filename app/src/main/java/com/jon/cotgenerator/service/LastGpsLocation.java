package com.jon.cotgenerator.service;

import android.location.Location;
import android.os.Build;

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
        if (lastLocation != null) {
            cot.lat = lastLocation.getLatitude();
            cot.lon = lastLocation.getLongitude();
            cot.hae = lastLocation.hasAltitude() ? lastLocation.getAltitude() : ZERO;
            cot.ce = lastLocation.hasAccuracy() ? lastLocation.getAccuracy() : UNKNOWN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cot.le = lastLocation.hasVerticalAccuracy() ? lastLocation.getVerticalAccuracyMeters() : UNKNOWN;
            } else {
                cot.le = UNKNOWN;
            }
        } else {
            cot.ce = UNKNOWN;
            cot.le = UNKNOWN;
        }
    }

    static void updatePli(PliCursorOnTarget cot) {
        updateCot(cot);
        if (lastLocation != null) {
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

    static double latitude() {
        return lastLocation != null ? lastLocation.getLatitude() : ZERO;
    }

    static double longitude() {
        return lastLocation != null ? lastLocation.getLongitude() : ZERO;
    }
}
