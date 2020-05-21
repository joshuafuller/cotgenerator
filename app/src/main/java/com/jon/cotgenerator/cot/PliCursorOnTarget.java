package com.jon.cotgenerator.cot;

import android.os.Build;

import androidx.annotation.NonNull;

import com.jon.cotgenerator.BuildConfig;

public class PliCursorOnTarget extends CursorOnTarget {
    // Group
    public String team = null;  // cyan, green, purple, etc
    public CotRole role = CotRole.TEAM_MEMBER;  // HQ, sniper, K9, etc

    // Location source
    public String altsrc = null;
    public String geosrc = null;

    // System info
    public Integer battery = null; // internal device battery remaining, scale of 1-100
    public String device = String.format("%s %s", Build.MANUFACTURER.toUpperCase(), Build.MODEL.toUpperCase());
    public String platform = "COT-GENERATOR";
    public String os = String.valueOf(Build.VERSION.SDK_INT);
    public String version = BuildConfig.VERSION_NAME;

    public PliCursorOnTarget() {
        how = CotHow.MG.get();
        type = CotType.GROUND_COMBAT.get();
    }

    @NonNull
    @Override
    public String toString() {
        boolean includeEndpoint = endpoint == null;
        String contact = includeEndpoint ?
                String.format(locale, "<contact callsign=\"%s\" endpoint=\"%s\"/>", callsign, endpoint) :
                String.format(locale, "<contact callsign=\"%s\"/>", callsign);

        String precloc = "", status = "", takv = "";
        if (altsrc != null && geosrc != null) {
            precloc = String.format(locale, "<precisionlocation altsrc=\"%s\" geopointsrc=\"%s\" />", altsrc, geosrc);
        }
        if (battery != null) {
            status = String.format(locale, "<status battery=\"%d\"/>", battery);
        }
        if (device != null && platform != null && os != null && version != null) {
            takv = String.format(locale,
                    "<takv device=\"%s\" platform=\"%s\" os=\"%s\" version=\"%s\"/>",
                    device, platform, os, version);
        }
        return String.format(locale,
                "<event version=\"2.0\" uid=\"%s\" type=\"%s\" time=\"%s\" start=\"%s\" stale=\"%s\" how=\"%s\"><point lat=\"%.7f\" " +
                        "lon=\"%.7f\" hae=\"%f\" ce=\"%f\" le=\"%f\"/><detail><track speed=\"%.7f\" course=\"%.7f\"/>%s" +
                        "<__group name=\"%s\" role=\"%s\"/>%s%s%s</detail></event>",
                uid, type, time.toString(), start.toString(), stale.toString(), how, lat,
                lon, hae, ce, le, speed, course, contact, team, role.toString(), precloc, status, takv);
    }
}
