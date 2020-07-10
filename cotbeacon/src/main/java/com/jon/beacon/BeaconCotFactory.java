package com.jon.beacon;

import android.content.SharedPreferences;

import com.jon.common.cot.CotRole;
import com.jon.common.cot.CotTeam;
import com.jon.common.cot.CursorOnTarget;
import com.jon.common.cot.UtcTimestamp;
import com.jon.common.service.CotFactory;
import com.jon.common.utils.DeviceUid;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeaconCotFactory extends CotFactory {
    private CursorOnTarget cot = null;

    public BeaconCotFactory(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    protected List<CursorOnTarget> generate() {
        return (cot == null) ? initialise() : update();
    }

    @Override
    protected List<CursorOnTarget> initialise() {
        cot = new CursorOnTarget();
        cot.uid = DeviceUid.get();
        cot.callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        cot.role = CotRole.fromPrefs(prefs);
        cot.team = CotTeam.fromPrefs(prefs);
        updateTime(cot);
        updateGpsData();
        return Collections.singletonList(cot);
    }

    @Override
    protected List<CursorOnTarget> update() {
        updateTime(cot);
        updateGpsData();
        return Collections.singletonList(cot);
    }

    @Override
    protected void clear() {
        cot = null;
    }

    private void updateTime(CursorOnTarget cot) {
        final UtcTimestamp now = UtcTimestamp.now();
        cot.time = now;
        cot.start = now;
        cot.setStaleDiff(PrefUtils.getInt(prefs, Key.STALE_TIMER), TimeUnit.MINUTES);
    }

    private void updateGpsData() {
        cot.lat = gpsCoords.latitude();
        cot.lon = gpsCoords.longitude();
        cot.hae = gpsCoords.altitude();
        cot.course = gpsCoords.bearing();
        cot.speed = gpsCoords.speed();
        cot.ce = gpsCoords.circularError90();
        cot.le = gpsCoords.linearError90();
        cot.altsrc = gpsCoords.gpsSource();
        cot.geosrc = gpsCoords.gpsSource();
    }

}
