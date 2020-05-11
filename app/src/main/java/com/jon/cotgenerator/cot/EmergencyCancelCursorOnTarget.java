package com.jon.cotgenerator.cot;

import androidx.annotation.NonNull;

public class EmergencyCancelCursorOnTarget extends CursorOnTarget {

    public EmergencyCancelCursorOnTarget() {
        how = CotHow.HE.get();
        type = CotType.EMERGENCY_CANCEL.get();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(locale,
                "<event how=\"%s\" stale=\"%s\" start=\"%s\" time=\"%s\" type=\"%s\" uid=\"%s-9-1-1\" version=\"2.0\"><point ce=\"%.7f\" " +
                        "hae=\"%f\" lat=\"%.7f\" le=\"%f\" lon=\"%f\" /><detail><emergency cancel=\"true\">%s</emergency></detail></event>",
                how, stale.toString(), start.toString(), time.toString(), type,
                uid, ce, hae, lat, le, lon, callsign);
    }
}
