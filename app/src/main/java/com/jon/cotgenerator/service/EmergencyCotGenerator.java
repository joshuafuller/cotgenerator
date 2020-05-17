package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.EmergencyCancelCursorOnTarget;
import com.jon.cotgenerator.cot.EmergencyCursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.concurrent.TimeUnit;

class EmergencyCotGenerator extends GpsCotGenerator {

    EmergencyCotGenerator(SharedPreferences prefs) {
        super(prefs);
    }

    EmergencyCursorOnTarget getEmergency() {
        EmergencyCursorOnTarget cot = new EmergencyCursorOnTarget();
        cot.uid = DeviceUid.get();
        cot.callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        cot.time = now;
        cot.start = now;
        cot.setStaleDiff(10, TimeUnit.MINUTES);
        LastGpsLocation.updateCot(cot);
        return cot;
    }

    EmergencyCancelCursorOnTarget getEmergencyCancel() {
        EmergencyCancelCursorOnTarget cot = new EmergencyCancelCursorOnTarget();
        cot.uid = DeviceUid.get();
        cot.callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        cot.time = now;
        cot.start = now;
        cot.setStaleDiff(0, TimeUnit.MINUTES);
        LastGpsLocation.updateCot(cot);
        return cot;
    }
}
