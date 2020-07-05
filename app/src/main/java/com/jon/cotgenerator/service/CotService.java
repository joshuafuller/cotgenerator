package com.jon.cotgenerator.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.ui.CotActivity;
import com.jon.cotgenerator.utils.GenerateInt;
import com.jon.cotgenerator.utils.Notify;
import com.jon.cotgenerator.utils.PrefUtils;

import timber.log.Timber;

public class CotService extends Service {
    public class ServiceBinder extends Binder {
        public CotService getService() { return CotService.this; }
    }

    public enum State { RUNNING, STOPPED, ERROR }

    public interface StateListener { void onStateChanged(CotService.State state, @Nullable Throwable throwable); }

    private static final String BASE_INTENT_ID = BuildConfig.APPLICATION_ID + ".CotService.";
    private static final int LAUNCH_ACTIVITY_PENDING_INTENT = GenerateInt.next();
    private static final int STOP_SERVICE_PENDING_INTENT = GenerateInt.next();
    public static final String START_SERVICE = BASE_INTENT_ID + "START";
    public static final String STOP_SERVICE = BASE_INTENT_ID + "STOP";

    private int updateRateSeconds;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;
            for (Location location : locationResult.getLocations()) {
                LastGpsLocation.update(location);
            }
        }
    };

    private CotManager cotManager;
    private SharedPreferences prefs;
    private IBinder binder = new ServiceBinder();
    private State state = State.STOPPED;
    private StateListener stateListener;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cotManager = new CotManager(prefs);
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(LastGpsLocation::update);
            initialiseLocationRequest();
        } catch (SecurityException e) {
            Timber.e(e);
            Timber.e("Failed to initialise Fused Location Client");
            error(e);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_SERVICE: start(); break;
                case STOP_SERVICE:  stop();  break;
            }
        }
        return Service.START_STICKY;
    }

    public void start() {
        Timber.i("Starting service");
        state = State.RUNNING;
        stateListener.onStateChanged(state, null);
        cotManager.start();
        startForegroundService();
    }

    public void stop() {
        Timber.i("Stopping service");
        state = State.STOPPED;
        stateListener.onStateChanged(state, null);
        cotManager.shutdown();
        stopForegroundService();
    }

    public void error(Throwable throwable) {
        Timber.e("Error in the service: %s", throwable.getMessage());
        state = State.STOPPED;
        stateListener.onStateChanged(State.ERROR, throwable);
        cotManager.shutdown();
        Notify.toast(this, "Uncaught error in service: " + throwable.getMessage());
        stopForegroundService();
    }

    public void updateGpsPeriod(int newPeriodSeconds) {
        updateRateSeconds = newPeriodSeconds;
        initialiseLocationRequest();
    }

    private void startForegroundService() {
        /* Intent to launch main activity when tapping the notification */
        PendingIntent launchPendingIntent = PendingIntent.getActivity(
                this,
                LAUNCH_ACTIVITY_PENDING_INTENT,
                new Intent(this, CotActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        /* Intent to stop the service when the notification button is tapped */
        Intent stopIntent = new Intent(this, CotService.class).setAction(CotService.STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, STOP_SERVICE_PENDING_INTENT, stopIntent, 0);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            Timber.e("NotificationManager == null");
            return;
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(PrefUtils.getPresetInfoString(prefs))
                .setContentIntent(launchPendingIntent)
                .addAction(R.drawable.stop, getString(R.string.stop), stopPendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BuildConfig.APPLICATION_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        } else {
            notification.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        startForeground(3, notification.build());
    }

    private void stopForegroundService() {
        state = State.STOPPED;
        stopForeground(true);
        stopSelf();
    }

    private void initialiseLocationRequest() {
        unregisterGpsUpdates();
        locationRequest = LocationRequest.create()
                .setInterval(updateRateSeconds * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        registerGpsUpdates();
    }

    private void registerGpsUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Timber.e(e);
            Timber.e("Failed to request location update from Fused Location Client");
            error(e);
        }
    }

    private void unregisterGpsUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public State getState() { return state; }

    public void registerStateListener(StateListener listener) {
        this.stateListener = listener;
        listener.onStateChanged(state, null);
    }

    public void unregisterStateListener() {
        stateListener = null;
    }
}
