package com.jon.cotgenerator

import android.content.SharedPreferences
import androidx.preference.Preference
import com.jon.common.ui.main.MainFragment
import com.jon.common.utils.InputValidator
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils

class GeneratorFragment : MainFragment() {

    override fun getPhoneInputKeys() = super.getPhoneInputKeys().apply {
        add(Key.MOVEMENT_SPEED)
        add(Key.RADIAL_DISTRIBUTION)
    }

    override fun getSuffixes() = super.getSuffixes().apply {
        put(Key.MOVEMENT_SPEED, "mph")
        put(Key.RADIAL_DISTRIBUTION, "metres")
    }

    override fun getPrefValidationRationales() = super.getPrefValidationRationales().apply {
        put(Key.CALLSIGN, "Contains invalid character(s)")
        put(Key.ICON_COUNT, "Should be an integer from 1 to 9999")
        put(Key.CENTRE_LATITUDE, "Should be a number between -90 and +90")
        put(Key.CENTRE_LONGITUDE, "Should be a number between -180 and +180")
        put(Key.RADIAL_DISTRIBUTION, "Should be a positive integer")
        put(Key.MOVEMENT_SPEED, "Should be a positive number")
    }

    override fun getSeekbarKeys() = super.getSeekbarKeys().apply {
        add(Key.CENTRE_ALTITUDE)
    }

    override fun updatePreferences() {
        /* If any toggles are enabled, hide the accompanying custom setting boxes */
        toggleCallsignSettingVisibility()
        toggleColourPickerVisibility()
        toggleRoleSettingVisibility()
        toggleLatLonSettingsVisibility()
        toggleAltitudeSettingVisibility()

        /* Fetch presets from the database */
        super.updatePreferences()
    }

    private fun toggleCallsignSettingVisibility() {
        val randomCallsignEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_CALLSIGNS)
        setPrefVisibleIfCondition(Key.CALLSIGN, !randomCallsignEnabled)
    }

    private fun toggleColourPickerVisibility() {
        /* The Colour Picker option should only be visible if Random Colours is disabled  */
        val randomColoursEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_COLOUR)
        setPrefVisibleIfCondition(Key.TEAM_COLOUR, !randomColoursEnabled)
    }

    private fun toggleRoleSettingVisibility() {
        val randomRoleEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_ROLE)
        setPrefVisibleIfCondition(Key.ICON_ROLE, !randomRoleEnabled)
    }

    private fun toggleLatLonSettingsVisibility() {
        val followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION)
        setPrefVisibleIfCondition(Key.CENTRE_LATITUDE, !followGps)
        setPrefVisibleIfCondition(Key.CENTRE_LONGITUDE, !followGps)
    }

    private fun toggleAltitudeSettingVisibility() {
        val showAltitudeSetting = PrefUtils.getBoolean(prefs, Key.STAY_AT_GROUND_LEVEL)
        setPrefVisibleIfCondition(Key.CENTRE_ALTITUDE, !showAltitudeSetting)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(prefs, key)
        when (key) {
            Key.RANDOM_CALLSIGNS -> toggleCallsignSettingVisibility()
            Key.RANDOM_COLOUR -> toggleColourPickerVisibility()
            Key.RANDOM_ROLE -> toggleRoleSettingVisibility()
            Key.FOLLOW_GPS_LOCATION -> toggleLatLonSettingsVisibility()
            Key.STAY_AT_GROUND_LEVEL -> toggleAltitudeSettingVisibility()
        }
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any): Boolean {
        /* Check the base preferences first, but back out if they fail */
        if (!super.onPreferenceChange(pref, newValue)) {
            return false
        }
        val input = newValue as String
        val inputValidator = InputValidator()
        return when (val key = pref.key) {
            Key.ICON_COUNT -> errorIfInvalid(input, key, inputValidator.validateInt(input, 1, 9999))
            Key.CENTRE_LATITUDE -> errorIfInvalid(input, key, inputValidator.validateDouble(input, -90.0, 90.0))
            Key.CENTRE_LONGITUDE -> errorIfInvalid(input, key, inputValidator.validateDouble(input, -180.0, 180.0))
            Key.RADIAL_DISTRIBUTION -> errorIfInvalid(input, key, inputValidator.validateInt(input, 1, null))
            Key.MOVEMENT_SPEED -> errorIfInvalid(input, key, inputValidator.validateDouble(input, 0.0, null))
            else -> true
        }
    }

    companion object {
        fun getInstance() = GeneratorFragment()
    }
}
