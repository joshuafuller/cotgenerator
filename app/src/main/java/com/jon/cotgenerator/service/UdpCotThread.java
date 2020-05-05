package com.jon.cotgenerator.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;

final class UdpCotThread extends CotThread {
    private static final String TAG = UdpCotThread.class.getSimpleName();
    private DatagramSocket socket;

    UdpCotThread(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    @Override
    void shutdown() {
        super.shutdown();
        if (socket != null) {
            socket.close();
        }
        socket = null;
    }

    @Override
    public void run() {
        super.run();
        initialiseDestAddress();
        initialiseSocket();
        final int movementRadius = PrefUtils.getInt(prefs, Key.MOVEMENT_RADIUS);
        cotGenerator = CotGenerator.getFromPrefs(prefs);
        List<CursorOnTarget> icons = cotGenerator.generate();

        while (isRunning) {
            long startTime = System.currentTimeMillis();
            for (CursorOnTarget cot : icons) {
                sendToDestination(cot);
            }
            icons = cotGenerator.generate();
            waitUntilNextTransmission(startTime);
        }
    }

    private void initialiseDestAddress() {
        try {
            destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.UDP_IP));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error parsing destination address: " + prefs.getString(Key.UDP_IP, ""));
            shutdown();
        }
        destPort = PrefUtils.getInt(prefs, Key.UDP_PORT);
    }

    private void initialiseSocket() {
        try {
            if (destIp.isMulticastAddress()) {
                socket = new MulticastSocket();
            } else {
                socket = new DatagramSocket();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when building transmit UDP socket");
            shutdown();
        }
    }

    @Override
    void sendToDestination(CursorOnTarget cot) {
        Log.i(TAG, "Sending cot: " + cot);
        try {
            final byte[] buf = cot.toBytes();
            socket.send(new DatagramPacket(buf, buf.length, destIp, destPort));
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }

}
