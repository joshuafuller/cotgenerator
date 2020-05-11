package com.jon.cotgenerator.cot;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class CursorOnTarget {
    public String how = CotHow.MG.get();
    public String type = CotType.GROUND_COMBAT.get();

    // User info
    public String uid = null;      // unique ID of the device. Stays constant when changing callsign

    // Time info
    public UtcTimestamp time;    // time when the icon was created
    public UtcTimestamp start;   // time when the icon is considered valid
    public UtcTimestamp stale;   // time when the icon is considered invalid

    // Contact info
    public String callsign = null; // ATAK callsign
    public String endpoint = null; // "ip:port:protocol", describing where the packet came from

    // Position and movement info
    public double hae = 0.0;    // height above ellipsoid in metres
    public double lat = 0.0;    // latitude in decimal degrees
    public double lon = 0.0;    // longitude in decimal degrees
    public double ce = 0.0;     // circular (radial) error in metres. applies to 2D position only
    public double le = 0.0;     // linear error in metres. applies to altitude only
    public double course = 0.0; // ground bearing in decimal degrees
    public double speed = 0.0;  // ground velocity in m/s. Doesn't include altitude climb rate

    // Any other info
    public String xmlDetail = null;

    protected final Locale locale = Locale.ENGLISH; // just to keep android studio from whining at me when using String.format

    public CursorOnTarget() { /* blank */ }

    public byte[] toBytes() {
        return this.toString().getBytes();
    }

    public void setStaleDiff(final long dt, final TimeUnit timeUnit) {
        int multiplier = getTimeUnitMultiplier(timeUnit);
        stale = new UtcTimestamp(start.toLong() + (multiplier * dt));
    }

    private int getTimeUnitMultiplier(TimeUnit timeUnit) {
        switch (timeUnit) {
            case MILLISECONDS: return 1;
            case SECONDS:      return 1000;
            case MINUTES:      return 60 * 1000;
            case HOURS:        return 60 * 60 * 1000;
            default:           throw new IllegalArgumentException("Give me a proper TimeUnit, not " + timeUnit.name());
        }
    }
}
