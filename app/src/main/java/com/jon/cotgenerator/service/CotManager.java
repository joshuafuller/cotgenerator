package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.enums.TransmissionProtocol;

class CotManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = CotManager.class.getSimpleName();
    private SharedPreferences prefs;
    private CotThread thread;

    CotManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    private boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    void start() {
        prefs.registerOnSharedPreferenceChangeListener(this);
        boolean isUdp = TransmissionProtocol.fromPrefs(prefs) == TransmissionProtocol.UDP;
        thread = isUdp ? new UdpCotThread(prefs) : new TcpCotThread(prefs);
        thread.start();
    }

    void shutdown() {
        if (thread != null) {
            thread.shutdown();
            thread = null;
        }
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        /* If any preferences are changed, kill the thread and instantly reload with the new settings */
        if (isRunning()) {
            shutdown();
            start();
        }
    }
}
