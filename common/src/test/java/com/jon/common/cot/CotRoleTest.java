package com.jon.common.cot;

import com.jon.common.SharedPreferenceTest;
import com.jon.common.utils.Key;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CotRoleTest extends SharedPreferenceTest {
    private final List<CotRole> ALL_ROLES = Arrays.asList(CotRole.values());

    @Test
    public void get_Valid() {
        assertThat(CotRole.TEAM_MEMBER.get(), equalTo("Team Member"));
        assertThat(CotRole.TEAM_LEADER.get(), equalTo("Team Leader"));
        assertThat(CotRole.HQ.get(), equalTo("HQ"));
        assertThat(CotRole.SNIPER.get(), equalTo("Sniper"));
        assertThat(CotRole.MEDIC.get(), equalTo("Medic"));
        assertThat(CotRole.FORWARD_OBSERVER.get(), equalTo("Forward Observer"));
        assertThat(CotRole.RTO.get(), equalTo("RTO"));
        assertThat(CotRole.K9.get(), equalTo("K9"));
    }

    @Test
    public void fromString_Valid() {
        CotRole role = CotRole.fromString(CotRole.MEDIC.get());
        assertThat(ALL_ROLES, hasItem(role));
    }

    @Test
    public void fromString_AllValid() {
        for (CotRole role : CotRole.values()) {
            CotRole parsedRole = CotRole.fromString(role.get());
            assertThat(ALL_ROLES, hasItem(parsedRole));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_Invalid() {
        CotRole role = CotRole.fromString("INVALID ROLE");
    }

    @Test
    public void fromPrefs_RandomValid() {
        initSharedPrefs();
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_ROLE, true)
                .commit();
        CotRole role = CotRole.fromPrefs(sharedPreferences);
        assertThat(ALL_ROLES, hasItem(role));
    }

    @Test
    public void fromPrefs_RandomAllValid() {
        initSharedPrefs();
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_ROLE, true)
                .commit();
        for (int i = 0; i < 50; i++) {
            CotRole role = CotRole.fromPrefs(sharedPreferences);
            assertThat(ALL_ROLES, hasItem(role));
        }
    }

    @Test
    public void fromPrefs_SpecificValid() {
        initSharedPrefs();
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_ROLE, false)
                .putString(Key.ICON_ROLE, CotRole.HQ.get())
                .commit();
        CotRole role = CotRole.fromPrefs(sharedPreferences);
        assertThat(role, equalTo(CotRole.HQ));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromPrefs_SpecificInvalid() {
        initSharedPrefs();
        sharedPreferences.edit()
                .putBoolean(Key.RANDOM_ROLE, false)
                .putString(Key.ICON_ROLE, "INVALID ROLE")
                .commit();
        CotRole role = CotRole.fromPrefs(sharedPreferences);
    }
}
