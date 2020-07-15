package com.jon.common.cot;

import com.jon.common.SharedPreferenceTest;
import com.jon.common.utils.Key;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CotTeamTest extends SharedPreferenceTest {
    private final List<CotTeam> ALL_TEAMS = Arrays.asList(CotTeam.values());

    @Before
    public void before() {
        initSharedPrefs();
    }

    @Test
    public void fromPrefs_RandomValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, true)
                .commit();
        CotTeam team = CotTeam.fromPrefs(sharedPreferences);
        assertThat(ALL_TEAMS, hasItem(team));
    }

    @Test
    public void fromPrefs_RandomAllValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, true)
                .commit();
        for (int i = 0; i < 50; i++) {
            CotTeam team = CotTeam.fromPrefs(sharedPreferences);
            assertThat(ALL_TEAMS, hasItem(team));
        }
    }

    @Test
    public void fromPrefs_SpecificValid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, false)
                .putInt(Key.TEAM_COLOUR, colourToInteger(CotTeam.CYAN))
                .commit();
        CotTeam team = CotTeam.fromPrefs(sharedPreferences);
        assertThat(team, equalTo(CotTeam.CYAN));
        assertThat(team.get(), equalTo("Cyan"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromPrefs_SpecificInvalid() {
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_COLOUR, false)
                .putInt(Key.TEAM_COLOUR, 0x000000) // black, not an option
                .commit();
        CotTeam team = CotTeam.fromPrefs(sharedPreferences);
    }

    private int colourToInteger(CotTeam team) {
        return Integer.parseInt(team.hex().substring(2), 16);
    }
}
