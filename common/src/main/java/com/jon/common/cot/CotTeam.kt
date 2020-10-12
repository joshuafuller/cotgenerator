package com.jon.common.cot

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getIntFromPair
import java.util.*


@Suppress("unused")
enum class CotTeam(private val colourName: String, private val colourHex: String) {
    PURPLE("Purple", "FF800080"),
    MAGENTA("Magenta", "FFFF00FF"),
    MAROON("Maroon", "FF800000"),
    RED("Red", "FFFF0000"),
    ORANGE("Orange", "FFFF8000"),
    YELLOW("Yellow", "FFFFFF00"),
    WHITE("White", "FFFFFFFF"),
    GREEN("Green", "FF008009"),
    DARK_GREEN("Dark Green", "FF00620B"),
    CYAN("Cyan", "FF00FFFF"),
    TEAL("Teal", "FF008784"),
    BLUE("Blue", "FF0003FB"),
    DARK_BLUE("Dark Blue", "FF0000A0");

    override fun toString() = colourName

    fun toHexString(): String {
        return colourHex
    }

    companion object {
        private val random = Random()

        private fun fromHexString(hexString: String): CotTeam {
            return values().firstOrNull { it.colourHex == hexString }
                    ?: throw IllegalArgumentException("Unknown CoT team: $hexString")
        }

        @SuppressLint("DefaultLocale")
        fun fromPrefs(prefs: SharedPreferences, isRandom: Boolean = false): CotTeam {
            return if (isRandom) {
                values()[random.nextInt(values().size)]
            } else {
                val team = Integer.toHexString(prefs.getIntFromPair(CommonPrefs.TEAM_COLOUR)).toUpperCase()
                fromHexString(assertHexFormatting(team))
            }
        }

        /* In case we get an integer like 0x008009, which returns "8009" as a hex string. This is technically valid, but doesn't match any
         * of the expected hex strings above. So we prepend the required chars: "FF" alpha channel and any padding "0"s */
        @Suppress("CascadeIf")
        private fun assertHexFormatting(hex: String): String {
            return if (hex.length == 8) {
                hex
            } else if (hex.length == 6) {
                "FF$hex"
            } else {
                val hexBuilder = StringBuilder(hex)
                while (hexBuilder.length < 6) hexBuilder.insert(0, "0")
                hexBuilder.insert(0, "FF")
                hexBuilder.toString()
            }
        }
    }
}
