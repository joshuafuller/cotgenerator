package com.jon.cotgenerator;

import android.content.SharedPreferences;

import androidx.preference.Preference;

import com.jon.common.ui.ArrayUtils;
import com.jon.common.ui.MainFragment;
import com.jon.common.utils.InputValidator;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.util.HashMap;
import java.util.Map;

public class GeneratorFragment extends MainFragment {
    private GeneratorFragment() { /* blank */ }

    public static MainFragment getInstance() {
        return new GeneratorFragment();
    }

    @Override
    protected String[] getPhoneInputKeys() {
        return ArrayUtils.concatStrings(
                super.getPhoneInputKeys(),
                new String[] {
                        Key.MOVEMENT_SPEED,
                        Key.RADIAL_DISTRIBUTION,
                }
        );
    }

    @Override
    protected String[] getSeekbarKeys() {
        return ArrayUtils.concatStrings(
                super.getSeekbarKeys(),
                new String[] { Key.CENTRE_ALTITUDE }
        );
    }

    @Override
    protected Map<String, String> getSuffixes() {
        return new HashMap<String, String>() {{
            putAll(GeneratorFragment.super.getSuffixes());
            put(Key.MOVEMENT_SPEED, "mph");
            put(Key.RADIAL_DISTRIBUTION, "metres");
        }};
    }

    @Override
    protected Map<String, String> getPrefValidationRationales() {
        return new HashMap<String, String>() {{
            putAll(GeneratorFragment.super.getPrefValidationRationales());
            put(Key.ICON_COUNT, "Should be an integer from 1 to 9999");
            put(Key.CENTRE_LATITUDE, "Should be a number between -180 and +180");
            put(Key.CENTRE_LONGITUDE, "Should be a number between -90 and +90");
            put(Key.RADIAL_DISTRIBUTION, "Should be a positive integer");
            put(Key.MOVEMENT_SPEED, "Should be a positive number");
        }};
    }

    @Override
    protected void updatePreferences() {
        /* If any toggles are enabled, hide the accompanying custom setting boxes */
        toggleCallsignSettingVisibility();
        toggleColourPickerVisibility();
        toggleRoleSettingVisibility();
        toggleLatLonSettingsVisibility();
        toggleAltitudeSettingVisibility();

        /* Fetch presets from the database */
        super.updatePreferences();
    }

    private void toggleCallsignSettingVisibility() {
        boolean randomCallsignEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_CALLSIGNS);
        setPrefVisibleIfCondition(Key.CALLSIGN, !randomCallsignEnabled);
    }

    private void toggleColourPickerVisibility() {
        /* The Colour Picker option should only be visible if Random Colours is disabled  */
        boolean randomColoursEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_COLOUR);
        setPrefVisibleIfCondition(Key.TEAM_COLOUR, !randomColoursEnabled);
    }

    private void toggleRoleSettingVisibility() {
        boolean randomRoleEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_ROLE);
        setPrefVisibleIfCondition(Key.ICON_ROLE, !randomRoleEnabled);
    }

    private void toggleLatLonSettingsVisibility() {
        boolean followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        setPrefVisibleIfCondition(Key.CENTRE_LATITUDE, !followGps);
        setPrefVisibleIfCondition(Key.CENTRE_LONGITUDE, !followGps);
    }

    private void toggleAltitudeSettingVisibility() {
        boolean showAltitudeSetting = PrefUtils.getBoolean(prefs, Key.STAY_AT_GROUND_LEVEL);
        setPrefVisibleIfCondition(Key.CENTRE_ALTITUDE, !showAltitudeSetting);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        super.onSharedPreferenceChanged(prefs, key);
        switch (key) {
            case Key.RANDOM_CALLSIGNS:
                toggleCallsignSettingVisibility();
                break;
            case Key.RANDOM_COLOUR:
                toggleColourPickerVisibility();
                break;
            case Key.RANDOM_ROLE:
                toggleRoleSettingVisibility();
                break;
            case Key.FOLLOW_GPS_LOCATION:
                toggleLatLonSettingsVisibility();
                break;
            case Key.STAY_AT_GROUND_LEVEL:
                toggleAltitudeSettingVisibility();
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        /* Check the base preferences first, but back out if they fail */
        if (!super.onPreferenceChange(pref, newValue)) {
            return false;
        }
        final String input = (String) newValue;
        final String key = pref.getKey();
        switch (key) {
            case Key.ICON_COUNT:
                return errorIfInvalid(input, key, InputValidator.validateInt(input, 1, 9999));
            case Key.CENTRE_LATITUDE:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, -90.0, 90.0));
            case Key.CENTRE_LONGITUDE:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, -180.0, 180.0));
            case Key.RADIAL_DISTRIBUTION:
                return errorIfInvalid(input, key, InputValidator.validateInt(input, 1, null));
            case Key.MOVEMENT_SPEED:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, 0.0, null));
            default:
                return true;
        }
    }
}
