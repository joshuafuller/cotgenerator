package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.DataFormat;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;
import com.jon.cotgenerator.utils.Protocol;

import java.net.InetAddress;
import java.util.List;

abstract class CotThread extends Thread {
    protected final SharedPreferences prefs;
    protected volatile boolean isRunning = false;
    protected CotGenerator cotGenerator;
    protected DataFormat dataFormat;
    protected InetAddress destIp;
    protected int destPort;
    protected List<CursorOnTarget> cotIcons;

    abstract void sendToDestination(CursorOnTarget cot);

    static CotThread fromPrefs(SharedPreferences prefs) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        switch (protocol) {
            case UDP: return new UdpCotThread(prefs);
            case TCP: return new TcpCotThread(prefs);
            case SSL: return new SslCotThread(prefs);
            default: throw new IllegalArgumentException("Unexpected protocol: " + protocol);
        }
    }

    protected CotThread(SharedPreferences sharedPrefs) {
        prefs = sharedPrefs;
        dataFormat = DataFormat.fromPrefs(prefs);
        cotGenerator = CotGenerator.getFromPrefs(prefs);
        cotIcons = cotGenerator.generate();
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

    protected int periodMilliseconds() {
        return PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD) * 1000;
    }
}
