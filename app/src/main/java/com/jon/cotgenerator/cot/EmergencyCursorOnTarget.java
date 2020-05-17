package com.jon.cotgenerator.cot;

import androidx.annotation.NonNull;

public class EmergencyCursorOnTarget extends CursorOnTarget {

    public EmergencyCursorOnTarget() {
        how = CotHow.MG.get();
        type = CotType.EMERGENCY_SEND.get();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(locale,
                "<event how=\"%s\" stale=\"%s\" start=\"%s\" time=\"%s\" type=\"%s\" uid=\"%s-9-1-1\" version=\"2.0\">" +
                        "<point ce=\"%.7f\" hae=\"%f\" lat=\"%.7f\" le=\"%f\" lon=\"%f\" /><detail>" +
                        "<link type=\"%s\" uid=\"%s\" relation=\"p-p\"/>" +
                        "<contact callsign=\"%s-Alert\" /><emergency type=\"911 Alert\">%s</emergency></detail></event>",
                how, stale.toString(), start.toString(), time.toString(), type, uid, ce, hae, lat, le, lon,
                CotType.GROUND_COMBAT.get(), uid, callsign, callsign);
    }
}
