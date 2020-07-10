package com.jon.common.service;

import android.content.SharedPreferences;

class CotManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final SharedPreferences prefs;
    private CotThread thread;
    private ThreadErrorListener errorListener;
    private Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> errorListener.reportError(throwable);

    CotManager(SharedPreferences prefs, ThreadErrorListener errorListener) {
        this.prefs = prefs;
        this.errorListener = errorListener;
    }

    boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    void start() {
        prefs.registerOnSharedPreferenceChangeListener(this);
        thread = CotThread.fromPrefs(prefs);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        thread.start();
    }

    void shutdown() {
        if (thread != null) {
            thread.shutdown();
            thread = null;
        }
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
