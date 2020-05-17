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

class TcpCotThread extends CotThread {
    private static final String TAG = TcpCotThread.class.getSimpleName();

    private Socket socket;
    private OutputStream outputStream;

    TcpCotThread(SharedPreferences prefs) {
        super(prefs);
    }
    TcpCotThread(SharedPreferences prefs, CotGenerator generator) {
        super(prefs, generator);
    }

    @Override
    void shutdown() {
        super.shutdown();
        if (socket != null) {
            try {
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                /* do nothing */
            }
            outputStream = null;
            socket = null;
        }
    }

    @Override
    public void run() {
        super.run();
        initialiseDestAddress();
        openSocket();
        int bufferTimeMs = periodMilliseconds() / cotIcons.size();

        while (isRunning) {
            for (CursorOnTarget cot : cotIcons) {
                sendToDestination(cot);
                bufferSleep(bufferTimeMs);
            }
            cotIcons = cotGenerator.generate();
        }
        shutdown();
    }

    @Override
    protected void sendToDestination(CursorOnTarget cot) {
        try {
            outputStream.write(cot.toBytes());
            Log.i(TAG, "Sent cot: " + cot.toString());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            shutdown();
        }
    }

    protected void initialiseDestAddress() {
        try {
            destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.TCP_ADDRESS));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error parsing destination address: " + prefs.getString(Key.TCP_ADDRESS, ""));
            shutdown();
        }
        destPort = PrefUtils.parseInt(prefs, Key.TCP_PORT);
    }

    protected void openSocket() {
        try {
            socket = new Socket(destIp, destPort);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
