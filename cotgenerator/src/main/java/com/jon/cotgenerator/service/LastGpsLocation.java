package com.jon.cotgenerator.service;

import android.location.Location;

import androidx.annotation.Nullable;

class LastGpsLocation {
    private static final float ZERO = 0.0f;
    @Nullable
    private static Location lastLocation = null;

    static void update(@Nullable Location location) {
        lastLocation = location;
    }

    static double latitude() {
        return lastLocation != null ? lastLocation.getLatitude() : ZERO;
    }

    static double longitude() {
        return lastLocation != null ? lastLocation.getLongitude() : ZERO;
    }
}
