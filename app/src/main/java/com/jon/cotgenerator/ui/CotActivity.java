package com.jon.cotgenerator.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatDelegate;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.service.CotService;

import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;
public class CotActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }
            switch (intent.getAction()) {
                case CotService.CLOSE_SERVICE_INTERNAL:
                    invalidateOptionsMenu();
                    break;
            }
        }
    };
    private LocalBroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cot_activity);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow();
        }
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CotService.CLOSE_SERVICE_INTERNAL);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
        /* Requesting permissions */
        if (!EasyPermissions.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            EasyPermissions.requestPermissions(this, "Needed for accessing GPS data", 123, REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(broadcastReceiver);
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
        Intent intent = new Intent(this, CotService.class);
        switch (item.getItemId()) {
            case R.id.start:
                sendServiceIntent(intent, CotService.START_SERVICE);
                return true;
            case R.id.pause:
                sendServiceIntent(intent, CotService.STOP_SERVICE);
                return true;
            case R.id.about:
                about();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendServiceIntent(final Intent intent, final String intentId) {
        intent.setAction(intentId);
        startService(intent);
        invalidateOptionsMenu();
    }

    private void about() {
        String msg = String.format(Locale.ENGLISH,
                "Version:\n\t%s\n\nBuild Time:\n\t%s",
                BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME);
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int buttonId) -> dialog.dismiss())
                .show();
    }
}
