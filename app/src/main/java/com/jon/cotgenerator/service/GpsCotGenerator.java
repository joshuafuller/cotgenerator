package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.PliCursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.enums.TeamColour;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.Collections;
import java.util.List;

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
        cot = new PliCursorOnTarget();
        cot.uid = DeviceUid.get();
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
        return Collections.singletonList(c);
    }
}
