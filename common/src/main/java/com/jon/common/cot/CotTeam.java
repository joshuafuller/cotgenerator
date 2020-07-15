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

    private static final Random random = new Random();
    final private String colourName;
    final private String colourHex;

    CotTeam(String name, String hex) {
        colourName = name;
        colourHex = hex;
    }

    public String get() { return colourName; }
    String hex() { return colourHex; }

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
            return values()[random.nextInt(values().length)];
        } else {
            String team = Integer.toHexString(PrefUtils.getInt(prefs, Key.TEAM_COLOUR)).toUpperCase();
            return fromString(assertHexFormatting(team));
        }
    }

    /* In case we get an integer like 0x008009, which returns "8009" as a hex string. This is technically valid, but doesn't match any
     * of the expected hex strings above. So we prepend the required chars: "FF" alpha channel and any padding "0"s
    * doesn't match */
    private static String assertHexFormatting(String hex) {
        if (hex.length() == 8) {
            return hex;
        } else if (hex.length() == 6) {
            return "FF" + hex;
        } else if (hex.length() > 6) {
            throw new IllegalArgumentException("Malformed hex string: " + hex);
        }

        StringBuilder hexBuilder = new StringBuilder(hex);
        while (hexBuilder.length() < 6)
            hexBuilder.insert(0, "0");
        hexBuilder.insert(0, "FF");
        return hexBuilder.toString();
    }
}