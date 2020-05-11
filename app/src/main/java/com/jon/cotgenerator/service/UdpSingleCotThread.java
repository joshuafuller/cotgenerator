package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;

class UdpSingleCotThread extends TcpCotThread {
    private CursorOnTarget cot;

    UdpSingleCotThread(SharedPreferences prefs, CursorOnTarget cot) {
        super(prefs);
        this.cot = cot;
    }

    @Override
    public void run() {
        initialiseDestAddress();
        openSocket();
        sendToDestination(cot);
        shutdown();
    }
}
