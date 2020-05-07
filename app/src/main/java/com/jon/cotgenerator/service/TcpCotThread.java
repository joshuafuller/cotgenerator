package com.jon.cotgenerator.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

final class TcpCotThread extends CotThread {
    private static final String TAG = TcpCotThread.class.getSimpleName();

    private Socket socket;
    private OutputStream outputStream;

    TcpCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    void shutdown() {
        super.shutdown();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }

    @Override
    public void run() {
        super.run();
        initialiseDestAddress();
        openSocket();
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

    @Override
    void sendToDestination(CursorOnTarget cot) {
        try {
            outputStream.write(cot.toBytes());
            Log.i(TAG, "Sent cot: " + cot.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialiseDestAddress() {
        try {
            destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.TCP_IP));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error parsing destination address: " + prefs.getString(Key.TCP_IP, ""));
            shutdown();
        }
        destPort = PrefUtils.parseInt(prefs, Key.TCP_PORT);
    }

    private void openSocket() {
        try {
            socket = new Socket(destIp, destPort);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
