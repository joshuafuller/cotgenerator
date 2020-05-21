package com.jon.cotgenerator.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.Protocol;
import com.jon.cotgenerator.enums.TransmittedData;
import com.jon.cotgenerator.utils.GenerateInt;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.OutputPreset;
import com.jon.cotgenerator.utils.PrefUtils;
import com.jon.cotgenerator.utils.PresetSqlHelper;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener,
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences prefs;
    private PresetSqlHelper sqlHelper;

    private static final String[] GPS_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int GPS_PERMISSION_CODE = GenerateInt.next();

    static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private static final String[] PHONE_INPUT = new String[]{
            Key.CENTRE_LATITUDE,
            Key.CENTRE_LONGITUDE,
            Key.ICON_COUNT,
            Key.MOVEMENT_SPEED,
            Key.RADIAL_DISTRIBUTION,
    };

    private static final Map<String, String> SUFFIXES = new HashMap<String, String>() {{
        put(Key.CENTRE_LATITUDE, "degrees");
        put(Key.CENTRE_LONGITUDE, "degrees");
        put(Key.MOVEMENT_SPEED, "mph");
        put(Key.RADIAL_DISTRIBUTION, "metres");
    }};

    private static final Map<String, String> PREFS_REQUIRING_VALIDATION = new HashMap<String, String>() {{
        put(Key.CALLSIGN, "Should only contain alphanumeric characters");
        put(Key.CENTRE_LATITUDE, "Should be a number between -180 and +180");
        put(Key.CENTRE_LONGITUDE, "Should be a number between -90 and +90");
        put(Key.ICON_COUNT, "Should be an integer from 1 to 9999");
        put(Key.MOVEMENT_SPEED, "Should be a positive number");
        put(Key.RADIAL_DISTRIBUTION, "Should be a positive integer");
    }};

    private static final String[] SEEKBARS = new String[]{
            Key.STALE_TIMER,
            Key.TRANSMISSION_PERIOD
    };

    @Override
    public void onCreatePreferences(Bundle savedState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        EditTextPreference.OnBindEditTextListener phoneInputType = (EditText text) -> text.setInputType(InputType.TYPE_CLASS_PHONE);
        for (final String key : PHONE_INPUT) {
            EditTextPreference pref = findPreference(key);
            if (pref != null) pref.setOnBindEditTextListener(phoneInputType);
        }

        for (final String key : PREFS_REQUIRING_VALIDATION.keySet()) {
            Preference pref = findPreference(key);
            if (pref != null) pref.setOnPreferenceChangeListener(this);
        }
        for (final String key : SEEKBARS) {
            SeekBarPreference seekbar = findPreference(key);
            seekbar.setMin(1); /* I can't set the minimum in the XML for whatever reason, so here it is */
        }
        setNewPresetListener();
        sqlHelper = new PresetSqlHelper(requireContext());
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
        setColourPickerActive();
        setPositionPrefsActive();
        requestGpsPermissionIfSet();
        updatePresetEntries(Protocol.UDP, Key.UDP_PRESETS);
        updatePresetEntries(Protocol.TCP, Key.TCP_PRESETS);
        Protocol newProtocol = Protocol.fromPrefs(prefs);
        insertPresetAddressAndPort(newProtocol == Protocol.TCP ? Key.TCP_PRESETS : Key.UDP_PRESETS);
    }

    @Override
    public void onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        sqlHelper.close();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (SUFFIXES.containsKey(key)) {
            setPreferenceSuffix(this.prefs, key, SUFFIXES.get(key));
        }
        switch (key) {
            case Key.TRANSMISSION_PROTOCOL:
                toggleProtocolSettingVisibility();
                Protocol newProtocol = Protocol.fromPrefs(prefs);
                insertPresetAddressAndPort(newProtocol == Protocol.TCP ? Key.TCP_PRESETS : Key.UDP_PRESETS);
                break;
            case Key.TRANSMITTED_DATA:
                toggleDataTypeSettingsVisibility();
                requestGpsPermissionIfSet();
                break;
            case Key.FOLLOW_GPS_LOCATION:
                setPositionPrefsActive();
                requestGpsPermissionIfSet();
                break;
            case Key.TCP_PRESETS:
            case Key.UDP_PRESETS:
                insertPresetAddressAndPort(key);
                break;
            case Key.RANDOM_COLOUR:
                setColourPickerActive();
                break;
            case Key.NEW_PRESET_ADDED:
                updatePresetEntries(Protocol.UDP, Key.UDP_PRESETS);
                updatePresetEntries(Protocol.TCP, Key.TCP_PRESETS);
                break;
        }
    }

    private void setPositionPrefsActive() {
        boolean followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        findPreference(Key.CENTRE_LATITUDE).setEnabled(!followGps);
        findPreference(Key.CENTRE_LONGITUDE).setEnabled(!followGps);
    }

    private void insertPresetAddressAndPort(String key) {
        EditTextPreference addressPref = findPreference(Key.DEST_ADDRESS);
        EditTextPreference portPref = findPreference(Key.DEST_PORT);
        ListPreference presetPref = findPreference(key);
        if (addressPref != null && portPref != null && presetPref != null) {
            OutputPreset preset = OutputPreset.fromString(presetPref.getValue());
            if (preset != null) {
                addressPref.setText(preset.address);
                portPref.setText(Integer.toString(preset.port));
            } else {
                presetPref.setValue(null);
                addressPref.setText(null);
                portPref.setText(null);
            }
        }
    }

    private void requestGpsPermissionIfSet() {
        boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        boolean fakeIconsFollowGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        if (sendGps || fakeIconsFollowGps) {
            if (!EasyPermissions.hasPermissions(requireContext(), GPS_PERMISSION)) {
                String rationale = "The GPS permission is required to access the device's location.";
                EasyPermissions.requestPermissions(this, rationale, GPS_PERMISSION_CODE, GPS_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == GPS_PERMISSION_CODE) {
            Notify.green(requireView(), "GPS Permission successfully granted");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == GPS_PERMISSION_CODE) {
            /* Change the GPS preference back to fake icons */
            ListPreference transmittedDataPref = findPreference(Key.TRANSMITTED_DATA);
            transmittedDataPref.setValue(TransmittedData.FAKE.get());
            SwitchPreference followGpsPref = findPreference(Key.FOLLOW_GPS_LOCATION);
            followGpsPref.setChecked(false);
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), GPS_PERMISSION[0])) {
                /* Permission has been permanently denied */
                View.OnClickListener action = view -> {
                    Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                };
                String msg = "GPS permission denied! Open the Android Settings to adjust permissions manually.";
                Notify.red(requireView(), msg, action, "OPEN");
            } else {
                /* Permission has been temporarily denied, so we can re-ask within the app */
                Notify.orange(requireView(), "GPS Permission is required for this setting. Re-select it to try again!");
            }
        }
    }

    private void toggleProtocolSettingVisibility() {
        boolean showUdpSettings = Protocol.fromPrefs(prefs) == Protocol.UDP;
        findPreference(Key.UDP_PRESETS).setVisible(showUdpSettings);
        findPreference(Key.TCP_PRESETS).setVisible(!showUdpSettings);
    }

    private void toggleDataTypeSettingsVisibility() {
        boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        findPreference(Key.ICON_COUNT).setVisible(!sendGps);
        findPreference(Key.LOCATION_GROUP).setVisible(!sendGps);
        findPreference(Key.ICON_ROLE).setVisible(sendGps);
    }

    private void setColourPickerActive() {
        boolean useRandomColours = PrefUtils.getBoolean(prefs, Key.RANDOM_COLOUR);
        Preference colourPicker = findPreference(Key.TEAM_COLOUR);
        if (colourPicker != null) {
            colourPicker.setEnabled(!useRandomColours);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        final String str = (String) newValue;
        boolean result = true;
        switch (pref.getKey()) {
            case Key.CALLSIGN:
                /* alphanumeric characters only */
                result = str.matches("\\w*?");
                break;
            case Key.CENTRE_LATITUDE:
                result = InputValidator.validateDouble(str, -90.0, 90.0);
                break;
            case Key.CENTRE_LONGITUDE:
                result = InputValidator.validateDouble(str, -180.0, 180.0);
                break;
            case Key.ICON_COUNT:
                result = InputValidator.validateInt(str, 1, 9999);
                break;
            case Key.RADIAL_DISTRIBUTION:
            case Key.TRANSMISSION_PERIOD:
                result = InputValidator.validateInt(str, 1, null);
                break;
            case Key.MOVEMENT_SPEED:
                result = InputValidator.validateDouble(str, 0.0, null);
                break;
        }
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + str + ". " + PREFS_REQUIRING_VALIDATION.get(pref.getKey()));
        }
        return result;
    }

    private void setPreferenceSuffix(final SharedPreferences prefs, final String key, final String suffix) {
        Preference pref = findPreference(key);
        if (pref != null) {
            String val = prefs.getString(key, "");
            pref.setSummary(String.format(Locale.ENGLISH, "%s %s", val, suffix));
        }
    }

    private void setNewPresetListener() {
        Preference addPreference = findPreference(Key.ADD_NEW_PRESET);
        if (addPreference != null) {
            addPreference.setOnPreferenceClickListener(clickedPref -> {
                NewPresetDialogCreator.show(requireContext(), requireView(), prefs, sqlHelper);
                return true;
            });
        }
        Preference deletePreference = findPreference(Key.DELETE_PRESET);
        if (deletePreference != null) {
            deletePreference.setOnPreferenceClickListener(clickedPref -> {
                deletePresetDialog();
                return true;
            });
        }
    }

    private void updatePresetEntries(Protocol protocol, String key) {
        List<OutputPreset> defaults = (protocol == Protocol.TCP) ? OutputPreset.tcpDefaults() : OutputPreset.udpDefaults();
        List<OutputPreset> presets = ListUtils.union(defaults, sqlHelper.getAllPresets(protocol));
        List<String> entries = OutputPreset.getAliases(presets);
        List<String> entryValues = new ArrayList<>();
        for (OutputPreset preset : presets) {
            entryValues.add(preset.toString());
        }
        ListPreference preference = findPreference(key);
        if (preference != null) {
            String previousValue = preference.getValue();
            preference.setEntries(Arrays.copyOf(entries.toArray(), entries.toArray().length, String[].class));
            preference.setEntryValues(Arrays.copyOf(entryValues.toArray(), entryValues.toArray().length, String[].class));
            preference.setValue(previousValue);
        }
    }

    private void deletePresetDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Presets")
                .setMessage("At the moment I haven't got a method for deleting individual output presets. This button takes you to the " +
                        "Android Settings screen for this app, so you can clear the storage (AKA remove all preferences/presets) and start from " +
                        "scratch. I'll (probably) fix this in a later version!")
                .setPositiveButton("SETTINGS", (dialog, buttonId) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }).setNegativeButton(android.R.string.cancel, (dialog, buttonId) -> dialog.dismiss())
                .show();
    }
}
