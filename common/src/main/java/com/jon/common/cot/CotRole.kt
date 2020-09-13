package com.jon.common.cot

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils
import java.util.*

enum class CotRole(private val role: String) {
    TEAM_MEMBER("Team Member"),
    TEAM_LEADER("Team Leader"),
    HQ("HQ"),
    SNIPER("Sniper"),
    MEDIC("Medic"),
    FORWARD_OBSERVER("Forward Observer"),
    RTO("RTO"),
    K9("K9");

    override fun toString() = role

    companion object {
        private val random = Random()

        @SuppressLint("DefaultLocale")
        fun fromString(roleString: String): CotRole {
            return values().firstOrNull { it.toString().toLowerCase() == roleString.toLowerCase() }
                    ?: throw IllegalArgumentException("Unknown CoT role: $roleString")
        }

        fun fromPrefs(prefs: SharedPreferences): CotRole {
            val useRandom = PrefUtils.getBoolean(prefs, Key.RANDOM_ROLE)
            return if (useRandom) {
                values()[random.nextInt(values().size)]
            } else {
                val role = PrefUtils.getString(prefs, Key.ICON_ROLE)
                fromString(role)
            }
        }
    }
}
