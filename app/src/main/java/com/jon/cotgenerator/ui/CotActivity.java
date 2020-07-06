package com.jon.cotgenerator.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.service.CotService;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.GenerateInt;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class CotActivity
        extends AppCompatActivity
        implements CotService.StateListener,
                   EasyPermissions.PermissionCallbacks,
                   SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PERMISSIONS_CODE = GenerateInt.next();
    public static final String[] PERMISSIONS = new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };

    private SharedPreferences prefs;

    private CotService service;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.i("onServiceConnected");
            service = ((CotService.ServiceBinder) binder).getService();
            service.registerStateListener(CotActivity.this);
            onStateChanged(service.getState(), null);

        }
        @Override public void onServiceDisconnected(ComponentName name) {
            Timber.i("onServiceDisconnected");
            service.unregisterStateListener();
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Regular setup */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cot_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow();
        }

        /* Start the service and bind to it */
        Intent intent = new Intent(this, CotService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        requestGpsPermission();

        if (service != null) {
            /* Refresh our state on resuming */
            onStateChanged(service.getState(), null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        if (service != null) {
            service.unregisterStateListener();
            service = null;
            if (serviceConnection != null) {
                unbindService(serviceConnection);
                serviceConnection = null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem start = menu.findItem(R.id.start);
        MenuItem stop = menu.findItem(R.id.stop);
        if (service != null) {
            start.setVisible(service.getState() == CotService.State.STOPPED);
            stop.setVisible(service.getState() == CotService.State.RUNNING);
        } else {
            start.setVisible(true);
            stop.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {
                    View.OnClickListener listener = view -> {
                        Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                    };
                    Notify.red(getRootView(), getString(R.string.permissionBegging), listener, "OPEN");
                    return true;
                }
                if (presetIsSelected()) {
                    service.start();
                    invalidateOptionsMenu();
                    Notify.green(getRootView(), "Service started");
                } else {
                    Notify.red(getRootView(), "Select an output destination first!");
                }
                return true;
            case R.id.stop:
                service.stop();
                invalidateOptionsMenu();
                Notify.blue(getRootView(), "Service stopped");
                return true;
            case R.id.about:
                AboutDialogCreator.show(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean presetIsSelected() {
        String presetKey = PrefUtils.getPresetPrefKeyFromSharedPrefs(prefs);
        return PrefUtils.getString(prefs, Key.DEST_ADDRESS).length() > 0
                && PrefUtils.getString(prefs, Key.DEST_PORT).length() > 0
                && PrefUtils.getString(prefs, presetKey).split(OutputPreset.SEPARATOR).length > 1;
    }

    private View getRootView() {
        return findViewById(android.R.id.content);
    }

    private void requestGpsPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permissionRationale), PERMISSIONS_CODE, PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSIONS_CODE) {
            Notify.green(getRootView(), "GPS Permission successfully granted");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSIONS_CODE) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[0])) {
                /* Permission has been permanently denied, so show a toast and open Android settings */
                Notify.toast(this, getString(R.string.permissionBegging));
                Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
            } else {
                /* Permission has been temporarily denied, so we can re-ask within the app */
                Notify.orange(getRootView(), getString(R.string.permissionRationale));
            }
        }
    }

    @Override
    public void onStateChanged(CotService.State state, @Nullable Throwable throwable) {
        invalidateOptionsMenu();
        if (state == CotService.State.ERROR && throwable != null) {
            Notify.red(getRootView(), "Error: " + throwable.getMessage());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Key.TRANSMISSION_PERIOD.equals(key)) {
            service.updateGpsPeriod(PrefUtils.getInt(prefs, key));
        }
    }
}
