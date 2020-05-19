package com.jon.cotgenerator.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.jon.cotgenerator.R;
import com.jon.cotgenerator.enums.TransmittedData;
import com.jon.cotgenerator.service.CotService;
import com.jon.cotgenerator.service.GpsService;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;
import com.leinardi.android.speeddial.SpeedDialView;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;

public class CotActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private LocalBroadcastManager broadcastManager;
    private SharedPreferences prefs;
    private ToggleButtonLayout toggleButtonLayout;
    private SpeedDialView speedDial;
    private SpeedDialView speedDialDisabled;
    private boolean serviceIsRunning;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(CotService.CLOSE_SERVICE_INTERNAL)) {
                serviceIsRunning = false;
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

        serviceIsRunning = isCotServiceRunning(this);
        speedDial = SpeedDialCreator.getSpeedDial(this);
        speedDialDisabled = SpeedDialCreator.getDisabledSpeedDial(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        /* Link the toggle button to a shared preference */
        toggleButtonLayout = findViewById(R.id.toggleButtonLayout);
        if (canUseFakeIcons()) {
            toggleButtonLayout.setOnToggledListener((toggle, selected, bool) -> {
                /* If the toggle value changes, set the shared preference appropriately */
                boolean selectedGps = selected.getId() == R.id.toggleGpsBeacon;
                String newValue = selectedGps ? TransmittedData.GPS.get() : TransmittedData.FAKE.get();
                prefs.edit().putString(Key.TRANSMITTED_DATA, newValue).apply();
                return null;
            });
        } else {
            /* Remove the option to select Fake Icons for API level <24 */
            toggleButtonLayout.setToggled(R.id.toggleGpsBeacon, true);
            toggleButtonLayout.setOnToggledListener((toggle, selected, bool) -> {
                /* If the toggle value changes, set the shared preference appropriately */
                Notify.red(findViewById(android.R.id.content), "This device has an API level of " + Build.VERSION.SDK_INT +
                        ". Fake Icons mode requires a minimum of " + Build.VERSION_CODES.N);
                toggleButtonLayout.setToggled(R.id.toggleGpsBeacon, true);
                return null;
            });
        }
        /* Use the current preference value to set the toggle */
        setToggleValueFromPreferences();
        toggleSpeedDialVisibility();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem start = menu.findItem(R.id.start);
        MenuItem stop = menu.findItem(R.id.stop);
        start.setVisible(!serviceIsRunning);
        stop.setVisible(serviceIsRunning);
        tintMenuIcon(start, android.R.color.holo_green_light);
        tintMenuIcon(stop, android.R.color.holo_red_light);
        return true;
    }

    private void tintMenuIcon(MenuItem item, @ColorRes int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(item.getIcon());
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, color));
        item.setIcon(wrapDrawable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        final boolean followGpsLocation = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        Intent cotIntent = new Intent(this, CotService.class);
        Intent gpsIntent = new Intent(this, GpsService.class);
        switch (item.getItemId()) {
            case R.id.start:
                if (presetIsSelected()) {
                    serviceIsRunning = true;
                    cotIntent.setAction(CotService.START_SERVICE);
                    startService(cotIntent);
                    if (sendGps || followGpsLocation) {
                        gpsIntent.setAction(GpsService.START_SERVICE);
                        startService(gpsIntent);
                    }
                    invalidateOptionsMenu();
                    toggleSpeedDialVisibility();
                } else {
                    Notify.red(findViewById(android.R.id.content), "Select an output destination first!");
                }
                return true;
            case R.id.stop:
                serviceIsRunning = false;
                cotIntent.setAction(CotService.STOP_SERVICE);
                startService(cotIntent);
                if (sendGps || followGpsLocation) {
                    gpsIntent.setAction(GpsService.STOP_SERVICE);
                    startService(gpsIntent);
                }
                invalidateOptionsMenu();
                toggleSpeedDialVisibility();
                return true;
            case R.id.about:
                AboutDialogCreator.show(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Key.TRANSMITTED_DATA)) {
            setToggleValueFromPreferences();
            toggleSpeedDialVisibility();
        }
    }

    private void setToggleValueFromPreferences() {
        if (canUseFakeIcons()) {
            boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
            toggleButtonLayout.setToggled(sendGps ? R.id.toggleGpsBeacon : R.id.toggleGenerator, true);
        }
    }

    private void toggleSpeedDialVisibility() {
        boolean sendGps = TransmittedData.fromPrefs(prefs) == TransmittedData.GPS;
        if (sendGps) {
            /* Show the active FAB only if the service is running */
            speedDial.setVisibility(serviceIsRunning ? View.VISIBLE : View.INVISIBLE);
            speedDialDisabled.setVisibility(serviceIsRunning ? View.INVISIBLE : View.VISIBLE);
        } else {
            /* No FABs unless GPS Beacon is selected */
            speedDial.setVisibility(View.INVISIBLE);
            speedDialDisabled.setVisibility(View.INVISIBLE);
        }
    }

    private boolean presetIsSelected() {
        return PrefUtils.getString(prefs, Key.DEST_ADDRESS).length() > 0
                && PrefUtils.getString(prefs, Key.DEST_PORT).length() > 0;
    }

    private boolean canUseFakeIcons() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
    }

}
