package com.jon.cotgenerator.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.presets.PresetRepository;
import com.jon.cotgenerator.service.CotService;
import com.jon.cotgenerator.utils.InputValidator;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;
import com.jon.cotgenerator.utils.Protocol;

public class EditPresetActivity
        extends ServiceBoundActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.toolbarHeaderEditPreset);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Fragment fragment = EditPresetFragment.newInstance();
        /* Pass all inter-activity arguments directly to the fragment */
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_preset_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save:
                if (settingsAreValid()) {
                    storePresetInDatabase();
                    passPresetBackToMainActivity();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.goBack)
                .setMessage(R.string.goBackMessage)
                .setPositiveButton(android.R.string.ok, (dialog, i) -> super.onBackPressed())
                .setNegativeButton(android.R.string.cancel, (dialog, i) -> dialog.dismiss())
                .show();
    }

    private boolean settingsAreValid() {
        try {
            /* Regular options */
            String protocolStr = PrefUtils.getString(prefs, Key.PRESET_PROTOCOL);
            String alias = PrefUtils.getString(prefs, Key.PRESET_ALIAS);
            String address = PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS);
            String portStr = PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT);
            if (!InputValidator.validateString(protocolStr, ".*?")) {
                Notify.orange(getRootView(), "No protocol chosen!");
                return false;
            } else if (!InputValidator.validateString(alias, ".*?")) {
                Notify.orange(getRootView(), "Enter an alias to describe the preset!");
                return false;
            } else if (!InputValidator.validateString(address, ".*?")) {
                Notify.orange(getRootView(), "No destination address has been entered!");
                return false;
            } else if (!InputValidator.validateInt(portStr, 1, 65535)) {
                Notify.orange(getRootView(), "Invalid port number!");
                return false;
            }

            Protocol protocol = getInputProtocol();
            if (protocol == Protocol.SSL) {
                /* SSL-only options */
                String clientBytes = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_BYTES);
                String clientPass = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_PASSWORD);
                String trustBytes = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_BYTES);
                String trustPass = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_PASSWORD);
                if (!InputValidator.validateString(clientBytes)) {
                    Notify.orange(getRootView(), "No client certificate chosen!");
                    return false;
                } else if (!InputValidator.validateString(clientPass)) {
                    Notify.orange(getRootView(), "No client certificate password entered!");
                    return false;
                } else if (!InputValidator.validateString(trustBytes)) {
                    Notify.orange(getRootView(), "No trust store chosen!");
                    return false;
                } else if (!InputValidator.validateString(trustPass)) {
                    Notify.orange(getRootView(), "No trust store password entered!");
                    return false;
                }
            }
        } catch (Exception e) {
            Notify.orange(getRootView(), "Unknown error when validating preset: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void storePresetInDatabase() {
        Protocol protocol = getInputProtocol();
        OutputPreset preset = new OutputPreset(
                protocol,
                PrefUtils.getString(prefs, Key.PRESET_ALIAS),
                PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS),
                Integer.parseInt(PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT))
        );
        if (protocol == Protocol.SSL) {
            preset.clientCert = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_BYTES).getBytes();
            preset.trustStore = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_BYTES).getBytes();
            preset.clientCertPassword = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_PASSWORD);
            preset.trustStorePassword = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_PASSWORD);
        }
        PresetRepository repository = PresetRepository.getInstance();
        repository.insertPreset(preset);
    }

    private void passPresetBackToMainActivity() {
        OutputPreset preset = new OutputPreset(
                getInputProtocol(),
                PrefUtils.getString(prefs, Key.PRESET_ALIAS),
                PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS),
                Integer.parseInt(PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT))
        );
        Intent intent = new Intent();
        intent.setData(Uri.parse(preset.toString()));
        setResult(RESULT_OK, intent);
        finish();
    }

    private View getRootView() {
        return findViewById(android.R.id.content);
    }

    private Protocol getInputProtocol() {
        return Protocol.fromString(PrefUtils.getString(prefs, Key.PRESET_PROTOCOL));
    }

    @Override
    public void onStateChanged(CotService.State state, @Nullable Throwable throwable) {
        if (state == CotService.State.ERROR && throwable != null) {
            Notify.red(getRootView(), "Error: " + throwable.getMessage());
        }
    }
}
