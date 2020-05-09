package com.jon.cotgenerator.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.TransmittedData;
import com.jon.cotgenerator.service.CotService;
import com.jon.cotgenerator.service.GpsService;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.Key;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class CotActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = CotActivity.class.getSimpleName();

    private LocalBroadcastManager broadcastManager;
    private SharedPreferences prefs;
    private ToggleButtonLayout toggleButtonLayout;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(CotService.CLOSE_SERVICE_INTERNAL)) {
                invalidateOptionsMenu();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Regular setup */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cot_activity);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow();
        }

        /* Receiving intents from services */
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CotService.CLOSE_SERVICE_INTERNAL);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        /* Generate a device-specific UUID and save to file, if it doesn't already exist */
        DeviceUid.generate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        /* Link the toggle button to a shared preference */
        toggleButtonLayout = findViewById(R.id.toggleButtonLayout);
        toggleButtonLayout.setOnToggledListener((toggle, selected, bool) -> {
            /* If the toggle value changes, set the shared preference appropriately */
            boolean selectedGps = selected.getId() == R.id.toggleGpsBeacon;
            String newValue = selectedGps ? TransmittedData.GPS.get() : TransmittedData.FAKE.get();
            prefs.edit().putString(Key.TRANSMITTED_DATA, newValue).apply();
            return null;
        });
        /* Use the current preference value to set the toggle */
        setToggleValueFromPreferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    private boolean isCotServiceRunning(Context context) {
        final String cotService = CotService.class.getName();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        /* Note that getRunningServices() is deprecated and currently only returns the services associated
         * with the current application. But that's all we need anyway, so whatever */
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (cotService.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void createFragTransaction(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (isCotServiceRunning(this)) {
            menu.findItem(R.id.start).setVisible(false);
            menu.findItem(R.id.pause).setVisible(true);
        } else {
            menu.findItem(R.id.start).setVisible(true);
            menu.findItem(R.id.pause).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        switch (item.getItemId()) {
            case R.id.start:
                sendServiceIntent(new Intent(this, CotService.class), CotService.START_SERVICE);
                if (sendGps) {
                    sendServiceIntent(new Intent(this, GpsService.class), GpsService.START_SERVICE);
                }
                return true;
            case R.id.pause:
                sendServiceIntent(new Intent(this, CotService.class), CotService.STOP_SERVICE);
                if (sendGps) {
                    sendServiceIntent(new Intent(this, GpsService.class), GpsService.STOP_SERVICE);
                }
                return true;
            case R.id.about:
                AboutDialogCreator.show(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendServiceIntent(final Intent intent, final String intentId) {
        intent.setAction(intentId);
        startService(intent);
        invalidateOptionsMenu();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Key.TRANSMITTED_DATA)) {
            setToggleValueFromPreferences();
        }
    }

    private void setToggleValueFromPreferences() {
        boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        toggleButtonLayout.setToggled(sendGps ? R.id.toggleGpsBeacon : R.id.toggleGenerator, true);
    }

}
