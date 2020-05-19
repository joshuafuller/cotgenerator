package com.jon.cotgenerator.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;

public class GpsService extends Service {
    /* Intent IDs */
    private static final String BASE_INTENT_ID = BuildConfig.APPLICATION_ID + ".GpsService.";
    public static final String START_SERVICE = BASE_INTENT_ID + "START";
    public static final String STOP_SERVICE = BASE_INTENT_ID + "STOP";
    private static final String CHANGE_UPDATE_RATE = BASE_INTENT_ID + "CHANGE_UPDATE_RATE";
    private static final String NEW_UPDATE_RATE_SECONDS = BASE_INTENT_ID + "NEW_UPDATE_RATE_SECONDS";

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

    @Override
    public IBinder onBind(Intent intent) {
        /* TODO: Return the communication channel to the service */
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(LastGpsLocation::update);
        initialiseLocationRequest();
    }

    private void initialiseLocationRequest() {
        unregisterGpsUpdates();
        locationRequest = LocationRequest.create()
                .setInterval(updateRateSeconds * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        registerGpsUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_SERVICE:
                    updateRateSeconds = Integer.parseInt(getString(R.string.defaultGpsUpdateRate));
                    registerGpsUpdates();
                    break;
                case STOP_SERVICE:
                    unregisterGpsUpdates();
                    stopForeground(true);
                    stopSelf();
                    break;
                case CHANGE_UPDATE_RATE:
                    updateRateSeconds = intent.getIntExtra(NEW_UPDATE_RATE_SECONDS, Integer.parseInt(getString(R.string.defaultGpsUpdateRate)));
                    initialiseLocationRequest();
                    break;
            }
        }
        return Service.START_STICKY;
    }

    private void registerGpsUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void unregisterGpsUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
