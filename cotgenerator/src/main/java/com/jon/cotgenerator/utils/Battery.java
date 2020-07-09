package com.jon.cotgenerator.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class Battery {
    private static Battery instance;
    private Intent batteryStatus;

    private Battery() { /* blank */ }

    synchronized public static Battery getInstance() {
        if (instance == null) {
            instance = new Battery();
        }
        return instance;
    }

    public void initialise(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, intentFilter);
    }

    public int getPercentage() {
        final int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        final int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return Math.round(level * 100 / (float)scale);
    }
}
