package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

class CotManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = CotManager.class.getSimpleName();
    private SharedPreferences mPrefs;
    private CotThread mThread;

    CotManager(SharedPreferences prefs) {
        mPrefs = prefs;
    }

    boolean isRunning() {
        return mThread != null && mThread.isRunning();
    }

    void start() {
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mThread = new CotThread(mPrefs);
        mThread.start();
    }

    void shutdown() {
        mThread.shutdown();
        mThread = null;
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (isRunning()) {
            shutdown();
            start();
        }
    }
}
