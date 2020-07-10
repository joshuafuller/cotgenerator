package com.jon.common.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jon.common.R;
import com.jon.common.presets.OutputPreset;
import com.jon.common.utils.FileUtils;
import com.jon.common.utils.GenerateInt;
import com.jon.common.utils.InputValidator;
import com.jon.common.utils.Key;
import com.jon.common.utils.Notify;
import com.jon.common.utils.PrefUtils;
import com.jon.common.utils.Protocol;
import com.jon.common.utils.UriUtils;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class EditPresetFragment
        extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
    private static final int CLIENT_CERT_FILE_REQUEST_CODE = GenerateInt.next();
    private static final int TRUST_STORE_FILE_REQUEST_CODE = GenerateInt.next();

    private static final String[] ALL_TEXT_PREFS = new String[] {
            Key.PRESET_ALIAS,
            Key.PRESET_DESTINATION_ADDRESS,
            Key.PRESET_DESTINATION_PORT,
            Key.PRESET_SSL_CLIENTCERT_PASSWORD,
            Key.PRESET_SSL_TRUSTSTORE_PASSWORD,
    };

    private static final String[] ALL_BYTES_PREFS = new String[] {
            Key.PRESET_SSL_CLIENTCERT_BYTES,
            Key.PRESET_SSL_TRUSTSTORE_BYTES
    };

    private static final int PASSWORD_INPUT_TYPE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    private static final String[] PASSWORD_PREFS = new String[] {
            Key.PRESET_SSL_CLIENTCERT_PASSWORD,
            Key.PRESET_SSL_TRUSTSTORE_PASSWORD
    };

    private static final Map<String, String> PREFS_REQUIRING_VALIDATION = new HashMap<String, String>() {{
        put(Key.PRESET_DESTINATION_ADDRESS, "Should be a valid network address");
        put(Key.PRESET_DESTINATION_PORT, "Should be an integer from 1-65355 inclusive");
    }};

    private SharedPreferences prefs;

    static OutputPreset initialPresetValues = null;

    static EditPresetFragment newInstance() {
        return new EditPresetFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.edit_preset, rootKey);

        /* Tell these two "bytes" preferences to launch a file browser when clicked */
        findPreference(Key.PRESET_SSL_CLIENTCERT_BYTES).setOnPreferenceClickListener(
                fileBrowserOnClickListener(CLIENT_CERT_FILE_REQUEST_CODE));
        findPreference(Key.PRESET_SSL_TRUSTSTORE_BYTES).setOnPreferenceClickListener(
                fileBrowserOnClickListener(TRUST_STORE_FILE_REQUEST_CODE));

        /* Set the specified fields to input as passwords. For god knows what reason, the inputType
         * XML field doesn't do this. */
        for (String key : PASSWORD_PREFS) {
            EditTextPreference pref = findPreference(key);
            if (pref != null) {
                pref.setOnBindEditTextListener(field -> field.setInputType(PASSWORD_INPUT_TYPE));
            }
        }

        /* URL input mode for the destination address */
        EditTextPreference destAddressPref = findPreference(Key.PRESET_DESTINATION_ADDRESS);
        if (destAddressPref != null) {
            destAddressPref.setOnBindEditTextListener(field -> field.setInputType(InputType.TYPE_TEXT_VARIATION_URI));
        }

        /* Phone input for port number, so digits only */
        EditTextPreference portPref = findPreference(Key.PRESET_DESTINATION_PORT);
        if (portPref != null) {
            portPref.setOnBindEditTextListener(field -> field.setInputType(InputType.TYPE_CLASS_PHONE));
        }

        /* Set all prefs requiring validation to apply checks before committing any changes */
        for (final String key : PREFS_REQUIRING_VALIDATION.keySet()) {
            Preference pref = findPreference(key);
            if (pref != null) pref.setOnPreferenceChangeListener(this);
        }


        prepopulateSpecifiedFields();
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("ConstantConditions")
    private void prepopulateSpecifiedFields() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        initialPresetValues = OutputPreset.blank();

        /* Protocol */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL)) {
            String protocolString = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL);
            ListPreference preference = findPreference(Key.PRESET_PROTOCOL);
            preference.setValue(protocolString);
            initialPresetValues.protocol = Protocol.fromString(protocolString);
        }
        /* Alias */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_ALIAS)) {
            String alias = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_ALIAS);
            EditTextPreference preference = findPreference(Key.PRESET_ALIAS);
            preference.setText(alias);
            initialPresetValues.alias = alias;
        }
        /* Address */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_ADDRESS)) {
            String address = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_ADDRESS);
            EditTextPreference preference = findPreference(Key.PRESET_DESTINATION_ADDRESS);
            preference.setText(address);
            initialPresetValues.address = address;
        }
        /* Port */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_PORT)) {
            int port = bundle.getInt(IntentIds.EXTRA_EDIT_PRESET_PORT);
            EditTextPreference preference = findPreference(Key.PRESET_DESTINATION_PORT);
            preference.setText(Integer.toString(port));
            initialPresetValues.port = port;
        }
        /* Client cert bytes */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES)) {
            final String byteString = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES);
            int length = InputValidator.validateString(byteString) ? byteString.length() : 0;
            Preference preference = findPreference(Key.PRESET_SSL_CLIENTCERT_BYTES);
            preference.setSummary(String.format("Loaded: %d bytes", length));
            prefs.edit().putString(Key.PRESET_SSL_CLIENTCERT_BYTES, byteString).apply();
            initialPresetValues.clientCert = byteString.getBytes();
        }
        /* Client cert password */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD)) {
            String password = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD);
            EditTextPreference preference = findPreference(Key.PRESET_SSL_CLIENTCERT_PASSWORD);
            preference.setText(password);
            initialPresetValues.clientCertPassword = password;
        }
        /* Trust store bytes */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES)) {
            final String byteString = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES);
            int length = InputValidator.validateString(byteString) ? byteString.length() : 0;
            Preference preference = findPreference(Key.PRESET_SSL_TRUSTSTORE_BYTES);
            preference.setSummary(String.format("Loaded: %d bytes", length));
            prefs.edit().putString(Key.PRESET_SSL_TRUSTSTORE_BYTES, byteString).apply();
            initialPresetValues.trustStore = byteString.getBytes();
        }
        /* Trust store password */
        if (bundle.containsKey(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD)) {
            String password = bundle.getString(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD);
            EditTextPreference preference = findPreference(Key.PRESET_SSL_TRUSTSTORE_PASSWORD);
            preference.setText(password);
            initialPresetValues.trustStorePassword = password;
        }
    }

    private Preference.OnPreferenceClickListener fileBrowserOnClickListener(int requestCode) {
        return preference -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/x-pkcs12"); // .p12 files only
            startActivityForResult(intent, requestCode);
            return true;
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        toggleSSLSettingsVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearPrefs();
    }

    @SuppressLint({"DefaultLocale", "ApplySharedPref"})
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent result) {
        /* Get the preference key corresponding to the request code */
        String key;
        if (requestCode == CLIENT_CERT_FILE_REQUEST_CODE) {
            key = Key.PRESET_SSL_CLIENTCERT_BYTES;
        } else if (requestCode == TRUST_STORE_FILE_REQUEST_CODE) {
            key = Key.PRESET_SSL_TRUSTSTORE_BYTES;
        } else {
            throw new RuntimeException("Unknown request code: " + requestCode);
        }

        if (resultCode != Activity.RESULT_OK) {
            return;
        } else {
            try {
                String path = UriUtils.getPathFromUri(requireContext(), result.getData());
                byte[] bytes = FileUtils.toByteArray(path);
                prefs.edit().putString(key, new String(bytes)).apply();
                findPreference(key).setSummary(String.format("Loaded: %d bytes", bytes.length));
            } catch (Exception e) {
                Notify.red(requireView(), "Failed: " + e.getMessage());
                Timber.e(e);
                prefs.edit().remove(key).apply();
                findPreference(key).setSummary(null);
            }
        }

        resetPassword(key);
    }

    private void resetPassword(String key) {
        EditTextPreference preference;
        if (Key.PRESET_SSL_CLIENTCERT_BYTES.equals(key)) {
            preference = findPreference(Key.PRESET_SSL_CLIENTCERT_PASSWORD);
        } else {
            preference = findPreference(Key.PRESET_SSL_TRUSTSTORE_PASSWORD);
        }
        if (preference != null) {
            preference.setText(null);
            preference.setSummary(null);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.i("onSharedPreferenceChanged %s", key);
        switch (key) {
            case Key.PRESET_SSL_CLIENTCERT_PASSWORD:
            case Key.PRESET_SSL_TRUSTSTORE_PASSWORD:
                setPasswordSummary(key);
                return;
            case Key.PRESET_PROTOCOL:
                toggleSSLSettingsVisibility();
                return;
        }
    }

    private void setPasswordSummary(String key) {
        EditTextPreference passwordPref = findPreference(key);
        if (passwordPref != null) {
            Timber.d("value = %s", passwordPref.getText());
            int length = passwordPref.getText().length();
            StringBuilder asterisks = new StringBuilder();
            for (int i = 0; i < length; i++) {
                asterisks.append("*");
            }
            passwordPref.setSummary(asterisks.toString());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String input = (String) newValue;
        final String key = preference.getKey();
        switch (key) {
            case Key.PRESET_DESTINATION_ADDRESS:
                return errorIfInvalid(input, key, InputValidator.validateHostname(input));
            case Key.PRESET_DESTINATION_PORT:
                return errorIfInvalid(input, key, InputValidator.validateInt(input, 1, 65535));
            default:
                return true;
        }
    }

    private boolean errorIfInvalid(String input, String key, boolean result) {
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + input + ". " + PREFS_REQUIRING_VALIDATION.get(key));
        }
        return result;
    }

    private void setPrefVisibleIfCondition(String key, boolean condition) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setVisible(condition);
        }
    }

    private void toggleSSLSettingsVisibility() {
        boolean sslIsSelected;
        try {
            sslIsSelected = Protocol.fromString(PrefUtils.getString(prefs, Key.PRESET_PROTOCOL)) == Protocol.SSL;
        } catch (Exception e) {
            /* thrown if the selected value is null */
            sslIsSelected = false;
        }
        setPrefVisibleIfCondition(Key.PRESET_SSL_OPTIONS_CATEGORY, sslIsSelected);
    }

    @SuppressLint("ApplySharedPref")
    private void clearPrefs() {
        SharedPreferences.Editor editor = prefs.edit();
        for (String textPrefKey : ALL_TEXT_PREFS) {
            editor.remove(textPrefKey);
            EditTextPreference preference = findPreference(textPrefKey);
            if (preference != null) preference.setText(null);
        }

        for (String bytesPrefKey : ALL_BYTES_PREFS) {
            editor.remove(bytesPrefKey);
            Preference preference = findPreference(bytesPrefKey);
            if (preference != null) preference.setSummary(null);
        }

        editor.remove(Key.PRESET_PROTOCOL);
        ListPreference protocolPref = findPreference(Key.PRESET_PROTOCOL);
        if (protocolPref != null) protocolPref.setValue(null);
        editor.commit();
    }
}
