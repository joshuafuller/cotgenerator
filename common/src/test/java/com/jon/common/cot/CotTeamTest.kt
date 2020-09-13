package com.jon.common.cot

import com.jon.common.SharedPreferencesTest
import com.jon.common.utils.Key
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test

@Suppress("PrivatePropertyName")
class CotTeamTest : SharedPreferencesTest() {
    private val ALL_TEAMS = listOf(*CotTeam.values())

    @Before
    fun before() {
        initSharedPrefs()
    }

    @Test
    fun fromPrefs_RandomValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, true)
                .commit()
        val team = CotTeam.fromPrefs(sharedPreferences)
        assertThat(ALL_TEAMS, hasItem(team))
    }

    @Test
    fun fromPrefs_RandomAllValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, true)
                .commit()
        for (i in 0..100) {
            val team = CotTeam.fromPrefs(sharedPreferences)
            assertThat(ALL_TEAMS, hasItem(team))
        }
    }

    @Test
    fun fromPrefs_SpecificValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, false)
                .putInt(Key.TEAM_COLOUR, colourToInteger(CotTeam.CYAN))
                .commit()
        val team = CotTeam.fromPrefs(sharedPreferences)
        assertThat(team, equalTo(CotTeam.CYAN))
        assertThat(team.toString(), equalTo("Cyan"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromPrefs_SpecificInvalid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, false)
                .putInt(Key.TEAM_COLOUR, 0x000000) // black, not an option
                .commit()
        val team = CotTeam.fromPrefs(sharedPreferences)
    }

    private fun colourToInteger(team: CotTeam): Int {
        return team.toHexString().substring(2).toInt(16)
    }
}
