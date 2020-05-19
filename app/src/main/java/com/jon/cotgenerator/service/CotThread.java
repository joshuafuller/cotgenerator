package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.enums.Protocol;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.net.InetAddress;
import java.util.List;

abstract class CotThread extends Thread {
    protected final SharedPreferences prefs;
    protected volatile boolean isRunning = false;
    protected CotGenerator cotGenerator;
    protected InetAddress destIp;
    protected int destPort;
    protected List<CursorOnTarget> cotIcons;

    abstract void sendToDestination(CursorOnTarget cot);

    static CotThread fromPrefs(SharedPreferences prefs) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        switch (protocol) {
            case UDP: return new UdpCotThread(prefs);
            case TCP: return new TcpCotThread(prefs);
            default: throw new IllegalArgumentException("Unexpected protocol: " + protocol);
        }
    }

    static CotThread getSingleCotThread(SharedPreferences prefs, CursorOnTarget cot) {
        Protocol protocol = Protocol.fromPrefs(prefs);
        switch (protocol) {
            case UDP: return new UdpSingleCotThread(prefs, cot);
            case TCP: return new TcpSingleCotThread(prefs, cot);
            default: throw new IllegalArgumentException("Unexpected protocol: " + protocol);
        }
    }

    protected CotThread(SharedPreferences prefs) {
        this.prefs = prefs;
        cotGenerator = CotGenerator.getFromPrefs(prefs);
        cotIcons = cotGenerator.generate();
    }

    protected CotThread(SharedPreferences prefs, CotGenerator generator) {
        this(prefs);
        cotGenerator = generator;
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
