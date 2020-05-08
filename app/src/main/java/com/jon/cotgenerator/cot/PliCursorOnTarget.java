package com.jon.cotgenerator.cot;

import androidx.annotation.NonNull;

import java.util.Locale;

public class PliCursorOnTarget extends CursorOnTarget {
    @NonNull
    @Override
    public String toString() {
        final Locale locale = Locale.ENGLISH; // just to keep android studio from whining at me

        /* the basic required fields in a CoT message */
        final String base = String.format(locale,
                "<event version=\"2.0\" uid=\"%s\" type=\"%s\" time=\"%s\" start=\"%s\" stale=\"%s\" how=\"%s\"><point lat=\"%.7f\" " +
                        "lon=\"%.7f\" hae=\"%f\" ce=\"%f\" le=\"%f\"/><detail><track speed=\"%.7f\" course=\"%.7f\"/>",
                uid, type, time.toString(), start.toString(), stale.toString(), how, lat,
                lon, hae, ce, le, speed, course);

        boolean includeEndpoint = endpoint == null;
        String contact = includeEndpoint ? String.format(locale, "<contact callsign=\"%s\" endpoint=\"%s\"/>", callsign, endpoint)
                : String.format(locale, "<contact callsign=\"%s\"/>", callsign);

        /* Create strings for the optional fields only if they have valid values */
        String group = "", status = "", takv = "", precloc = "";
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
        if (altsrc != null && geosrc != null) {
            precloc = String.format(locale, "<precisionlocation altsrc=\"%s\" geopointsrc=\"%s\" />", altsrc, geosrc);
        }

        return base + contact + group + status + takv + precloc + "</detail></event>";
    }
}
