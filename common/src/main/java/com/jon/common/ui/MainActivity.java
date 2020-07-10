package com.jon.common.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.jon.common.AppSpecific;
import com.jon.common.R;
import com.jon.common.presets.OutputPreset;
import com.jon.common.service.CotService;
import com.jon.common.utils.GenerateInt;
import com.jon.common.utils.Key;
import com.jon.common.utils.Notify;
import com.jon.common.utils.PrefUtils;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity
        extends ServiceBoundActivity
        implements EasyPermissions.PermissionCallbacks,
                   SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PERMISSIONS_CODE = GenerateInt.next();
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Regular setup */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(AppSpecific.getAppName());
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, AppSpecific.getMainFragment())
                    .commitNow();
        }
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
        final int resId = item.getItemId();
        if (resId == R.id.start) {
            if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {
                View.OnClickListener listener = view -> {
                    Uri uri = Uri.parse("package:" + AppSpecific.getAppId());
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                };
                Notify.red(getRootView(), getString(R.string.gpsPermissionBegging), listener, "OPEN");
            }
            if (presetIsSelected()) {
                service.start();
                invalidateOptionsMenu();
                Notify.green(getRootView(), "Service started");
            } else {
                Notify.red(getRootView(), "Select an output destination first!");
            }
        } else if (resId == R.id.stop) {
            service.stop();
            invalidateOptionsMenu();
            Notify.blue(getRootView(), "Service stopped");
        } else if (resId == R.id.about) {
            new AboutDialogCreator().show(this);
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean presetIsSelected() {
        String presetKey = PrefUtils.getPresetPrefKeyFromSharedPrefs(prefs);
        return PrefUtils.getString(prefs, Key.DEST_ADDRESS).length() > 0
                && PrefUtils.getString(prefs, Key.DEST_PORT).length() > 0
                && PrefUtils.getString(prefs, presetKey).split(OutputPreset.SEPARATOR).length > 1;
    }

    private void requestGpsPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMISSIONS)) {
            EasyPermissions.requestPermissions(this, AppSpecific.getPermissionRationale(), PERMISSIONS_CODE, PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSIONS_CODE) {
            Notify.green(getRootView(), "Permissions successfully granted");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == PERMISSIONS_CODE) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[0])) {
                /* GPS permission has been permanently denied, so show a toast and open Android settings */
                Notify.toast(this, getString(R.string.gpsPermissionBegging));
                Uri uri = Uri.parse("package:" + AppSpecific.getAppId());
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
            } else {
                /* Permission has been temporarily denied, so we can re-ask within the app */
                Notify.orange(getRootView(), getString(R.string.gpsPermissionRationale));
            }

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[1])) {
                /* Storage permission has been permanently denied, so show a toast and open Android settings */
                Notify.toast(this, getString(R.string.storagePermissionBegging));
                Uri uri = Uri.parse("package:" + AppSpecific.getAppId());
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
            } else {
                /* Storage permission has been temporarily denied, so we can re-ask within the app */
                Notify.orange(getRootView(), getString(R.string.storagePermissionRationale));
            }
        }
    }

    @Override
    public void onStateChanged(CotService.State state, @Nullable Throwable throwable) {
        invalidateOptionsMenu();
        super.onStateChanged(state, throwable);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Key.TRANSMISSION_PERIOD.equals(key)) {
            service.updateGpsPeriod(PrefUtils.getInt(prefs, key));
        }
    }
}
