package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.net.InetAddress;

abstract class CotThread extends Thread {
    protected SharedPreferences prefs;
    protected volatile boolean isRunning = false;
    protected CotGenerator cotGenerator;
    protected InetAddress destIp;
    protected int destPort;

    abstract void sendToDestination(CursorOnTarget cot);

    CotThread(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        isRunning = true;
    }

    void shutdown() {
        isRunning = false;
        cotGenerator.clear();
        interrupt();
    }

    protected void waitUntilNextTransmission(long startTime) {
        try {
            final int periodSeconds = PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD);
            final long dt = (periodSeconds * 1000) - (System.currentTimeMillis() - startTime);
            if (dt > 0) {
                Thread.sleep(dt);
            }
        } catch (InterruptedException e) {
            /* do nothing */
        }
    }
}
