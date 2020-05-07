package com.jon.cotgenerator.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.ServerPreset;
import com.jon.cotgenerator.enums.TransmissionProtocol;
import com.jon.cotgenerator.enums.TransmittedData;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.Key;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences prefs;
    private boolean shouldCheckTcpPresetsPreference = true;

    static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private static final String[] PHONE_INPUT = new String[]{
            Key.CENTRE_LATITUDE,
            Key.CENTRE_LONGITUDE,
            Key.UDP_IP,
            Key.UDP_PORT,
            Key.TCP_IP,
            Key.TCP_PORT,
            Key.ICON_COUNT,
            Key.MOVEMENT_RADIUS,
            Key.RADIAL_DISTRIBUTION,
    };

    private static final Map<String, String> SUFFIXES = new HashMap<String, String>() {{
        put(Key.CENTRE_LATITUDE, "degrees");
        put(Key.CENTRE_LONGITUDE, "degrees");
        put(Key.MOVEMENT_RADIUS, "metres");
        put(Key.RADIAL_DISTRIBUTION, "metres");
    }};

    private static final String[] KEYS_REQUIRING_VALIDATION = new String[]{
            Key.CALLSIGN,
            Key.CENTRE_LATITUDE,
            Key.CENTRE_LONGITUDE,
            Key.UDP_IP,
            Key.UDP_PORT,
            Key.TCP_IP,
            Key.TCP_PORT,
            Key.ICON_COUNT,
            Key.MOVEMENT_RADIUS,
            Key.RADIAL_DISTRIBUTION,
    };

    private static final String[] SEEKBARS = new String[]{
            Key.STALE_TIMER,
            Key.TRANSMISSION_PERIOD
    };

    private EditTextPreference.OnBindEditTextListener phone = (EditText text) -> text.setInputType(InputType.TYPE_CLASS_PHONE);

    @Override
    public void onCreatePreferences(Bundle savedState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        for (final String key : PHONE_INPUT) {
            EditTextPreference pref = findPreference(key);
            if (pref != null) pref.setOnBindEditTextListener(phone);
        }
        for (final String key : KEYS_REQUIRING_VALIDATION) {
            Preference pref = findPreference(key);
            if (pref != null) pref.setOnPreferenceChangeListener(this);
        }
        for (final String key : SEEKBARS) {
            SeekBarPreference seekbar = findPreference(key);
            seekbar.setMin(1); /* I can't set the minimum in the XML for whatever reason, so here it is */
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle state) {
        super.onActivityCreated(state);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Map.Entry entry : SUFFIXES.entrySet()) {
            setPreferenceSuffix(prefs, (String) entry.getKey(), (String) entry.getValue());
        }
        toggleProtocolSettingVisibility();
        toggleDataTypeSettingsVisibility();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(TAG, "onSharedPreferenceChanged " + key);
        if (SUFFIXES.containsKey(key)) {
            setPreferenceSuffix(this.prefs, key, SUFFIXES.get(key));
        }
        switch (key) {
            case Key.TRANSMISSION_PROTOCOL:
                toggleProtocolSettingVisibility();
                break;
            case Key.TRANSMITTED_DATA:
                toggleDataTypeSettingsVisibility();
                break;
            case Key.TCP_PRESETS:
                if (shouldCheckTcpPresetsPreference) {
                    insertPresetTcpServer();
                }
                break;
            case Key.TCP_IP:
            case Key.TCP_PORT:
                if (shouldCheckTcpPresetsPreference) {
                    shouldCheckTcpPresetsPreference = false;
                    ListPreference presetPref = findPreference(Key.TCP_PRESETS);
                    presetPref.setValue("");
                    shouldCheckTcpPresetsPreference = true;
                }
                break;
        }
    }

    private void insertPresetTcpServer() {
        shouldCheckTcpPresetsPreference = false;
        EditTextPreference addressPref = findPreference(Key.TCP_IP);
        EditTextPreference portPref = findPreference(Key.TCP_PORT);
        if (addressPref != null && portPref != null) {
            ServerPreset preset = ServerPreset.fromPrefs(prefs);
            preset.fillPreferences(addressPref, portPref);
        }
        shouldCheckTcpPresetsPreference = true;
    }

    private void toggleProtocolSettingVisibility() {
        boolean showUdpSettings = TransmissionProtocol.fromPrefs(prefs) == TransmissionProtocol.UDP;
        findPreference(Key.UDP_GROUP).setVisible(showUdpSettings);
        findPreference(Key.TCP_GROUP).setVisible(!showUdpSettings);
    }

    private void toggleDataTypeSettingsVisibility() {
        boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        findPreference(Key.ICON_COUNT).setVisible(!sendGps);
        findPreference(Key.LOCATION_GROUP).setVisible(!sendGps);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        Log.i(TAG, "onSharedPreferenceChanged " + pref.getKey());
        final String str = (String) newValue;
        boolean result = true;
        switch (pref.getKey()) {
            case Key.CALLSIGN:
                /* alphanumeric characters only */
                result = str.matches("\\w*?");
                break;
            case Key.CENTRE_LATITUDE:
                result = validateDouble(str, -90.0, 90.0);
                break;
            case Key.CENTRE_LONGITUDE:
                result = validateDouble(str, -180.0, 180.0);
                break;
            case Key.ICON_COUNT:
            case Key.MOVEMENT_RADIUS:
            case Key.RADIAL_DISTRIBUTION:
            case Key.TRANSMISSION_PERIOD:
                result = validateInt(str, 1, null);
                break;
            case Key.TCP_IP:
            case Key.UDP_IP:
                result = validateIpAddress(str);
                break;
            case Key.TCP_PORT:
            case Key.UDP_PORT:
                result = validateInt(str, 1, 65535);
                break;
        }
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + str);
        }
        return result;
    }

    private boolean validateInt(final String str, final Integer min, final Integer max) {
        try {
            int number = Integer.parseInt(str);
            return (min == null || number >= min) && (max == null || number <= max);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean validateDouble(final String str, final Double min, final Double max) {
        try {
            double number = Double.parseDouble(str);
            return (min == null || number >= min) && (max == null || number <= max);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean validateIpAddress(final String str) {
        /* I would use InetAddress.getByName here, but that requires a non-UI thread and I cba doing that */
        final String[] split = str.split("\\.");
        if (split.length != 4) {
            return false;
        }
        for (String s : split) {
            try {
                int value = Integer.parseInt(s);
                if (value > 255 || value < 0) return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    private void setPreferenceSuffix(final SharedPreferences prefs, final String key, final String suffix) {
        Preference pref = findPreference(key);
        if (pref == null) {
            Log.e(TAG, "Couldn't find preference \"" + key + "\"");
            return;
        }
        String val = prefs.getString(key, "");
        pref.setSummary(String.format(Locale.ENGLISH, "%s %s", val, suffix));
    }
}
