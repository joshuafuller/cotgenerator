package com.jon.cotgenerator.service;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.enums.TeamColour;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class GpsCotGenerator extends CotGenerator {
    private static final String TAG = GpsCotGenerator.class.getSimpleName();

    private CursorOnTarget cot = null;

    GpsCotGenerator(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    protected List<CursorOnTarget> generate() {
        return (cot == null) ? initialise() : update();
    }

    @Override
    protected List<CursorOnTarget> initialise() {
        cot = new CursorOnTarget();
        String uid;
        try {
            uid = UUID.fromString(Build.getSerial()).toString();
            Log.i(TAG, "serial = " + Build.getSerial());
        } catch (SecurityException e) {
            /* thrown if the user denies permission to READ_PHONE_STATE, which is required to get device serial number */
            uid = UUID.randomUUID().toString();
        }
        cot.uid = uid;
        cot.callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        cot.time = now;
        cot.start = now;
        cot.setStaleDiff(1000 * 60 * PrefUtils.getInt(prefs, Key.STALE_TIMER));
        cot.team = TeamColour.fromPrefs(prefs).team();
        LastGpsLocation.updateCot(cot);
        return result(cot);
    }

    @Override
    protected List<CursorOnTarget> update() {
        LastGpsLocation.updateCot(cot);
        return result(cot);
    }

    @Override
    protected void clear() {
        cot = null;
    }

    private List<CursorOnTarget> result(CursorOnTarget c) {
        return new ArrayList<CursorOnTarget>() {{
            add(c);
        }};
    }
}
