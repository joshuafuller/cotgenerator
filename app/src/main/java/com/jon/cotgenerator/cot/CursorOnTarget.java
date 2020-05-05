package com.jon.cotgenerator.cot;

import android.os.Build;
import android.util.Log;

import com.jon.cotgenerator.BuildConfig;

import java.util.Locale;

public class CursorOnTarget {
    private static final String TAG = CursorOnTarget.class.getSimpleName();

    public String how = "m-g";
    public String type = "a-f-G-U";

    // User info
    public String uid = null;      // unique ID of the device. Stays constant when changing callsign
    public String callsign = null; // ATAK callsign
    public String endpoint = null; // "ip:port:protocol", describing where the packet came from

    // Time info
    public UtcTimestamp time;    // time when the icon was created
    public UtcTimestamp start;   // time when the icon is considered valid
    public UtcTimestamp stale;   // time when the icon is considered invalid

    // Position and movement info
    public double hae = 0.0;    // height above ellipsoid in metres
    public double lat = 0.0;    // latitude in decimal degrees
    public double lon = 0.0;    // longitude in decimal degrees
    public double ce = 0.0;     // circular (radial) error in metres. applies to 2D position only
    public double le = 0.0;     // linear error in metres. applies to altitude only
    public double course = 0.0; // ground bearing in decimal degrees
    public double speed = 0.0;  // ground velocity in m/s. Doesn't include altitude climb rate

    // Group
    public String team = null;  // cyan, green, purple, etc
    public String role = "Team Member";  // HQ, sniper, K9, etc

    // System info
    public Integer battery = null; // internal device battery remaining, scale of 1-100
    public String device = String.format("%s %s", Build.MANUFACTURER.toUpperCase(), Build.MODEL.toUpperCase());
    public String platform = "COT-GENERATOR";
    public String os = String.valueOf(Build.VERSION.SDK_INT);
    public String version = BuildConfig.VERSION_NAME;

    // Any other info
    public String xmlDetail = null;

    public CursorOnTarget() { /* blank */ }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(final boolean includeEndpoint) {
        final Locale locale = Locale.ENGLISH; // just to keep android studio from whining at me

        /* the basic required fields in a CoT message */
        final String base = String.format(locale,
                "<?xml version=\"1.0\"?><event version=\"2.0\" uid=\"%s\" type=\"%s\" time=\"%s\"" +
                        " start=\"%s\" stale=\"%s\" how=\"%s\"><point lat=\"%.7f\" lon=\"%.7f\" hae=\"%f\"" +
                        " ce=\"%f\" le=\"%f\"/><detail><track speed=\"%.7f\" course=\"%.7f\"/>",
                uid, type, time.toString(), start.toString(), stale.toString(), how, lat,
                lon, hae, ce, le, speed, course);

        String contact = includeEndpoint ? String.format(locale, "<contact callsign=\"%s\" endpoint=\"%s\"/>", callsign, endpoint)
                                         : String.format(locale, "<contact callsign=\"%s\"/>", callsign);

        /* Create strings for the optional fields only if they have valid values */
        String group = "", status = "", takv = "";
        if (team != null && role != null) {
            group = String.format(locale, "<__group name=\"%s\" role=\"%s\"/>", team, role);
        }
        if (battery != null) {
            status = String.format(locale, "<status battery=\"%d\"/>", battery);
        }
        if (device != null && platform != null && os != null && version != null) {
            takv = String.format(locale,
                    "<takv device=\"%s\" platform=\"%s\" os=\"%s\" version=\"%s\"/>",
                    device, platform, os, version);
        }

        return base + contact + group + status + takv + "</detail></event>";
    }

    public byte[] toBytes() {
        return this.toString().getBytes();
    }

    public void setStaleDiff(final long dt) {
        stale = new UtcTimestamp(start.toLong() + dt);
    }
}
