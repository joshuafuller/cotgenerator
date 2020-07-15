package com.jon.common.cot;

import android.content.SharedPreferences;

import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.util.Random;

public enum CotRole {
    TEAM_MEMBER("Team Member"),
    TEAM_LEADER("Team Leader"),
    HQ("HQ"),
    SNIPER("Sniper"),
    MEDIC("Medic"),
    FORWARD_OBSERVER("Forward Observer"),
    RTO("RTO"),
    K9("K9");

    private static final Random random = new Random();
    private final String role;

    CotRole(String role) {
        this.role = role;
    }

    public String get() {
        return role;
    }

    public static CotRole fromString(String roleString) {
        for (CotRole role : values()) {
            if (role.get().equals(roleString)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown CoT role: " + roleString);
    }

    public static CotRole fromPrefs(SharedPreferences prefs) {
        final boolean useRandom = PrefUtils.getBoolean(prefs, Key.RANDOM_ROLE);
        if (useRandom) {
            return values()[random.nextInt(values().length)];
        } else {
            String role = PrefUtils.getString(prefs, Key.ICON_ROLE);
            return fromString(role);
        }
    }
}
