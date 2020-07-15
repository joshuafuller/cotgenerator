package com.jon.common.cot;

import android.content.SharedPreferences;

import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.util.Random;

public enum CotTeam {
    PURPLE(    "Purple",     "FF800080"),
    MAGENTA(   "Magenta",    "FFFF00FF"),
    MAROON(    "Maroon",     "FF800000"),
    RED(       "Red",        "FFFF0000"),
    ORANGE(    "Orange",     "FFFF8000"),
    YELLOW(    "Yellow",     "FFFFFF00"),
    WHITE(     "White",      "FFFFFFFF"),
    GREEN(     "Green",      "FF008009"),
    DARK_GREEN("Dark Green", "FF00620B"),
    CYAN(      "Cyan",       "FF00FFFF"),
    TEAL(      "Teal",       "FF008784"),
    BLUE(      "Blue",       "FF0003FB"),
    DARK_BLUE( "Dark Blue",  "FF0000A0");

    final private String colourName;
    final private String colourHex;

    CotTeam(String name, String hex) {
        colourName = name;
        colourHex = hex;
    }

    public String get() { return colourName; }

    private static CotTeam fromString(String teamString) {
        for (CotTeam team : values()) {
            if (team.colourHex.equals(teamString)) {
                return team;
            }
        }
        throw new IllegalArgumentException("Unknown CoT team: " + teamString);
    }

    public static CotTeam fromPrefs(final SharedPreferences prefs) {
        final boolean useRandom = PrefUtils.getBoolean(prefs, Key.RANDOM_COLOUR);
        if (useRandom) {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        } else {
            String team = Integer.toHexString(PrefUtils.getInt(prefs, Key.TEAM_COLOUR)).toUpperCase();
            return fromString(team);
        }
    }
}