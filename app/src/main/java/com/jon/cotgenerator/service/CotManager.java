package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.EmergencyCancelCursorOnTarget;
import com.jon.cotgenerator.cot.EmergencyCursorOnTarget;

class CotManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final SharedPreferences prefs;
    private CotThread thread;

    CotManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    private boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    void start() {
        prefs.registerOnSharedPreferenceChangeListener(this);
        thread = CotThread.fromPrefs(prefs);
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

    void startEmergency() {
        EmergencyCotGenerator generator = new EmergencyCotGenerator(prefs);
        EmergencyCursorOnTarget cot = generator.getEmergency();
        CotThread emergencyThread = CotThread.getSingleCotThread(prefs, cot);
        emergencyThread.start();
    }

    void cancelEmergency() {
        EmergencyCotGenerator generator = new EmergencyCotGenerator(prefs);
        EmergencyCancelCursorOnTarget cot = generator.getEmergencyCancel();
        CotThread cancellingThread = CotThread.getSingleCotThread(prefs, cot);
        cancellingThread.start();
    }
}
