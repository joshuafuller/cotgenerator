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
        if (cotGenerator != null) {
            cotGenerator.clear();
        }
        interrupt();
    }

    protected void bufferSleep(int bufferTimeMs) {
        try {
            Thread.sleep(bufferTimeMs);
        } catch (InterruptedException e) {
            /* do nothing */
        }
    }
}
