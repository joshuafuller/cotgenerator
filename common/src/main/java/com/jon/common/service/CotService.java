package com.jon.common.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
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
import com.jon.common.AppSpecific;
import com.jon.common.BuildConfig;
import com.jon.common.R;
import com.jon.common.ui.MainActivity;
import com.jon.common.utils.GenerateInt;
import com.jon.common.utils.Notify;
import com.jon.common.utils.PrefUtils;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

abstract public class CotService extends Service implements ThreadErrorListener {
    public class ServiceBinder extends Binder {
        public CotService getService() { return CotService.this; }
    }

    public enum State { RUNNING, STOPPED, ERROR }

    public interface StateListener { void onStateChanged(CotService.State state, @Nullable Throwable throwable); }

    private static final String BASE_INTENT_ID = BuildConfig.LIBRARY_PACKAGE_NAME  + ".CotService.";
    public static final String STOP_SERVICE = BASE_INTENT_ID + "STOP";
    private static final int LAUNCH_ACTIVITY_PENDING_INTENT = GenerateInt.next();
    private static final int STOP_SERVICE_PENDING_INTENT = GenerateInt.next();

    private int updateRateSeconds;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                gpsCoords.update(locationResult.getLastLocation());
            }
        }
    };

    private GpsCoords gpsCoords = GpsCoords.getInstance();
    private CotManager cotManager;
    private IBinder binder = new ServiceBinder();
    private State state = State.STOPPED;
    private Set<StateListener> stateListeners = new HashSet<>();

    protected SharedPreferences prefs;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cotManager = new CotManager(prefs, this);
    }

    protected void initialiseFusedLocationClient() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> gpsCoords.update(location));
            initialiseLocationRequest();
        } catch (SecurityException e) {
            Timber.e(e);
            Timber.e("Failed to initialise Fused Location Client");
            error(e);
        }
    }

    public void start() {
        Timber.i("Starting service");
        state = State.RUNNING;
        for (StateListener stateListener : stateListeners) {
            stateListener.onStateChanged(state, null);
        }
        cotManager.start();
        startForegroundService();
    }

    public void stop() {
        Timber.i("Stopping service");
        state = State.STOPPED;
        for (StateListener stateListener : stateListeners) {
            stateListener.onStateChanged(state, null);
        }
        cotManager.shutdown();
        stopForegroundService();
    }

    public void error(Throwable throwable) {
        Timber.e(throwable);
        state = State.STOPPED;
        Timber.i("%d listeners", stateListeners.size());
        if (stateListeners.isEmpty()) {
            /* No UI activities open, so post a toast which should(!) appear over any other apps in the foreground */
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Notify.toast(getApplicationContext(), throwable.getMessage()));
        } else {
            for (StateListener stateListener : stateListeners) {
                stateListener.onStateChanged(State.ERROR, throwable);
            }
        }
        cotManager.shutdown();
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
                new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK),
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

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, AppSpecific.getAppId())
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(AppSpecific.getAppName())
                .setContentText(PrefUtils.getPresetInfoString(prefs))
                .setContentIntent(launchPendingIntent)
                .addAction(R.drawable.stop, getString(R.string.stop), stopPendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppSpecific.getAppId(),
                    AppSpecific.getAppName(),
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
        stateListeners.add(listener);
        listener.onStateChanged(state, null);
    }

    public void unregisterStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    @Override
    public void reportError(Throwable throwable) {
        /* Get an error from a thread, so pass the message down to our activity and show the user */
        error(throwable);
    }
}
