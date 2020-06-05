package com.jon.cotgenerator.cot;

import android.content.SharedPreferences;

import com.jon.cotgenerator.utils.Key;

public enum CotRole {
    TEAM_MEMBER("Team Member"),
    TEAM_LEADER("Team Leader"),
    HQ("HQ"),
    SNIPER("Sniper"),
    MEDIC("Medic"),
    FORWARD_OBSERVER("Forward Observer"),
    RTO("RTO"),
    K9("K9");

    private final String value;

    CotRole(String how) {
        this.value = how;
    }

    public String get() {
        return value;
    }

    public static CotRole fromString(String role) {
        switch (role) {
            case "Team Member":      return TEAM_MEMBER;
            case "Team Leader":      return TEAM_LEADER;
            case "HQ":               return HQ;
            case "Sniper":           return SNIPER;
            case "Medic":            return MEDIC;
            case "Forward Observer": return FORWARD_OBSERVER;
            case "RTO":              return RTO;
            case "K9":               return K9;
            default:                 throw new IllegalArgumentException("Unknown CoT role: " + role);
        }
    }

    public static CotRole fromPrefs(SharedPreferences prefs) {
        String role = prefs.getString(Key.ICON_ROLE, "");
        return fromString(role);
    }
}
