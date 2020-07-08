package com.jon.cotgenerator.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.presets.PresetRepository;
import com.jon.cotgenerator.utils.InputValidator;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;
import com.jon.cotgenerator.utils.Protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class CotFragment
        extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
                   Preference.OnPreferenceChangeListener {
    private SharedPreferences prefs;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    static CotFragment newInstance() {
        return new CotFragment();
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
        put(Key.CALLSIGN, "Contains invalid character(s)");
        put(Key.CENTRE_LATITUDE, "Should be a number between -180 and +180");
        put(Key.CENTRE_LONGITUDE, "Should be a number between -90 and +90");
        put(Key.ICON_COUNT, "Should be an integer from 1 to 9999");
        put(Key.MOVEMENT_SPEED, "Should be a positive number");
        put(Key.RADIAL_DISTRIBUTION, "Should be a positive integer");
    }};

    private static final String[] SEEKBARS = new String[]{
            Key.STALE_TIMER,
            Key.CENTRE_ALTITUDE,
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
            if (seekbar != null) seekbar.setMin(1); /* I can't set the minimum in the XML for whatever reason, so here it is */
        }
        setPresetPreferenceListeners();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle state) {
        super.onActivityCreated(state);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Map.Entry<String, String> entry : SUFFIXES.entrySet()) {
            setPreferenceSuffix(prefs, entry.getKey(), entry.getValue());
        }
        updatePreferences();
    }

    private void updatePreferences() {
        /* If any toggles are enabled, hide the accompanying custom setting boxes */
        toggleCallsignSettingVisibility();
        toggleRoleSettingVisibility();
        toggleProtocolSettingVisibility();
        toggleDataFormatSettingVisibility();
        toggleColourPickerVisibility();
        toggleAltitudeSettingVisibility();
        toggleLatLonSettingsVisibility();

        /* Fetch presets from the database */
        updatePresetPreferences();
        insertPresetAddressAndPort();
    }

    @Override
    public void onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (SUFFIXES.containsKey(key)) {
            setPreferenceSuffix(this.prefs, key, SUFFIXES.get(key));
        }
        switch (key) {
            case Key.RANDOM_CALLSIGNS:
                toggleCallsignSettingVisibility();
                break;
            case Key.RANDOM_ROLE:
                toggleRoleSettingVisibility();
                break;
            case Key.TRANSMISSION_PROTOCOL:
                toggleProtocolSettingVisibility();
                toggleDataFormatSettingVisibility();
                insertPresetAddressAndPort();
                break;
            case Key.FOLLOW_GPS_LOCATION:
                toggleLatLonSettingsVisibility();
                break;
            case Key.STAY_AT_GROUND_LEVEL:
                toggleAltitudeSettingVisibility();
                break;
            case Key.SSL_PRESETS:
            case Key.TCP_PRESETS:
            case Key.UDP_PRESETS:
                insertPresetAddressAndPort();
                break;
            case Key.RANDOM_COLOUR:
                toggleColourPickerVisibility();
                break;
            case Key.NEW_PRESET_ADDED:
                updatePresetPreferences();
                break;
        }
    }

    private void setPrefVisibleIfCondition(String key, boolean condition) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setVisible(condition);
        }
    }

    private void toggleLatLonSettingsVisibility() {
        boolean followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        setPrefVisibleIfCondition(Key.CENTRE_LATITUDE, !followGps);
        setPrefVisibleIfCondition(Key.CENTRE_LONGITUDE, !followGps);
    }

    private void toggleCallsignSettingVisibility() {
        boolean randomCallsignEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_CALLSIGNS);
        setPrefVisibleIfCondition(Key.CALLSIGN, !randomCallsignEnabled);
    }

    private void toggleRoleSettingVisibility() {
        boolean randomRoleEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_ROLE);
        setPrefVisibleIfCondition(Key.ICON_ROLE, !randomRoleEnabled);
    }

    private void toggleProtocolSettingVisibility() {
        final Protocol protocol = Protocol.fromPrefs(prefs);
        setPrefVisibleIfCondition(Key.SSL_PRESETS, protocol == Protocol.SSL);
        setPrefVisibleIfCondition(Key.TCP_PRESETS, protocol == Protocol.TCP);
        setPrefVisibleIfCondition(Key.UDP_PRESETS, protocol == Protocol.UDP);
    }

    private void toggleDataFormatSettingVisibility() {
        /* Data format is only relevant for UDP, since TAK Server only takes XML data */
        boolean showDataFormatSetting = Protocol.fromPrefs(prefs) == Protocol.UDP;
        setPrefVisibleIfCondition(Key.DATA_FORMAT, showDataFormatSetting);
    }

    private void toggleColourPickerVisibility() {
        /* The Colour Picker option should only be visible if Random Colours is disabled  */
        boolean randomColoursEnabled = PrefUtils.getBoolean(prefs, Key.RANDOM_COLOUR);
        setPrefVisibleIfCondition(Key.TEAM_COLOUR, !randomColoursEnabled);
    }

    private void toggleAltitudeSettingVisibility() {
        boolean showAltitudeSetting = PrefUtils.getBoolean(prefs, Key.STAY_AT_GROUND_LEVEL);
        setPrefVisibleIfCondition(Key.CENTRE_ALTITUDE, !showAltitudeSetting);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        final String input = (String) newValue;
        final String key = pref.getKey();
        switch (key) {
            case Key.CALLSIGN:
                return errorIfInvalid(input, key, InputValidator.validateCallsign(input));
            case Key.CENTRE_LATITUDE:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, -90.0, 90.0));
            case Key.CENTRE_LONGITUDE:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, -180.0, 180.0));
            case Key.ICON_COUNT:
                return errorIfInvalid(input, key, InputValidator.validateInt(input, 1, 9999));
            case Key.RADIAL_DISTRIBUTION:
            case Key.TRANSMISSION_PERIOD:
                return errorIfInvalid(input, key, InputValidator.validateInt(input, 1, null));
            case Key.MOVEMENT_SPEED:
                return errorIfInvalid(input, key, InputValidator.validateDouble(input, 0.0, null));
            default: return true;
        }
    }

    private boolean errorIfInvalid(String input, String key, boolean result) {
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + input + ". " + PREFS_REQUIRING_VALIDATION.get(key));
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

    private void setPresetPreferenceListeners() {
        Preference addPreference = findPreference(Key.ADD_NEW_PRESET);
        if (addPreference != null) {
            addPreference.setOnPreferenceClickListener(clickedPref -> {
                Intent intent = new Intent(getActivity(), EditPresetActivity.class);
                intent.putExtra(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL, PrefUtils.getString(prefs, Key.TRANSMISSION_PROTOCOL));
                startActivity(intent);
                return true;
            });
        }
        Preference deletePreference = findPreference(Key.DELETE_PRESETS);
        if (deletePreference != null) {
            deletePreference.setOnPreferenceClickListener(clickedPref -> {
                deletePresetDialog();
                return true;
            });
        }
    }

    private void insertPresetAddressAndPort() {
        String key;
        switch (Protocol.fromPrefs(prefs)) {
            case SSL: key = Key.SSL_PRESETS; break;
            case TCP: key = Key.TCP_PRESETS; break;
            case UDP: key = Key.UDP_PRESETS; break;
            default: throw new IllegalArgumentException("Unknown protocol: " + Protocol.fromPrefs(prefs));
        }
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

    private void updatePresetPreferences() {
        PresetRepository repository = PresetRepository.getInstance();
        compositeDisposable.add(repository.getByProtocol(Protocol.SSL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(presets -> updatePresetEntries(presets, Key.SSL_PRESETS), this::notifyError));
        compositeDisposable.add(repository.getByProtocol(Protocol.TCP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(presets -> updatePresetEntries(presets, Key.TCP_PRESETS), this::notifyError));
        compositeDisposable.add(repository.getByProtocol(Protocol.UDP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(presets -> updatePresetEntries(presets, Key.UDP_PRESETS), this::notifyError));
    }

    private void updatePresetEntries(List<OutputPreset> presets, String prefKey) {
        List<String> entries = OutputPreset.getAliases(presets);
        List<String> entryValues = new ArrayList<>();
        for (OutputPreset preset : presets) {
            entryValues.add(preset.toString());
        }
        ListPreference preference = findPreference(prefKey);
        if (preference != null) {
            String previousValue = preference.getValue();
            preference.setEntries(Arrays.copyOf(entries.toArray(), entries.toArray().length, String[].class));
            preference.setEntryValues(Arrays.copyOf(entryValues.toArray(), entryValues.toArray().length, String[].class));
            preference.setValue(previousValue);
        }
    }

    private void notifyError(Throwable throwable) {
        Notify.red(requireView(), "Error: " + throwable.getMessage());
        Timber.e(throwable);
    }

    private void deletePresetDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Presets")
                .setMessage("Clear all custom output presets? The built-in defaults will still remain.")
                .setPositiveButton(android.R.string.ok, (dialog, buttonId) -> {
                    PresetRepository repository = PresetRepository.getInstance();
                    repository.deleteDatabase();
                    resetPresetPreferences(repository, Protocol.SSL);
                    resetPresetPreferences(repository, Protocol.TCP);
                    resetPresetPreferences(repository, Protocol.UDP);
                }).setNegativeButton(android.R.string.cancel, (dialog, buttonId) -> dialog.dismiss())
                .show();
    }

    private void resetPresetPreferences(PresetRepository repository, Protocol protocol) {
        List<OutputPreset> defaults = repository.defaultsByProtocol(protocol);
        List<String> entries = OutputPreset.getAliases(defaults);
        List<String> values = new ArrayList<>();
        for (OutputPreset preset : defaults) {
            values.add(preset.toString());
        }
        ListPreference preference = findPreference(protocol == Protocol.TCP ? Key.TCP_PRESETS : Key.UDP_PRESETS);
        if (preference != null) {
            preference.setEntries(Arrays.copyOf(entries.toArray(), entries.size(), String[].class));
            preference.setEntryValues(Arrays.copyOf(values.toArray(), values.size(), String[].class));
            preference.setValueIndex(0);
        }
    }
}
