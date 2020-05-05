package com.jon.cotgenerator.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;

final class TcpCotThread extends CotThread {
    private static final String TAG = TcpCotThread.class.getSimpleName();

    TcpCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    void shutdown() {
        super.shutdown();
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            Log.i(TAG, "running");
        }
    }

    @Override
    void sendToDestination(CursorOnTarget cot) {

    }
}
