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

        int periodMilliseconds = PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD) * 1000;
        int bufferTimeMs = periodMilliseconds / icons.size();

        while (isRunning) {
            for (CursorOnTarget cot : icons) {
                sendToDestination(cot);
                bufferSleep(bufferTimeMs);
            }
            icons = cotGenerator.generate();
        }
    }

    @Override
    void sendToDestination(CursorOnTarget cot) {
        try {
            outputStream.write(cot.toBytes());
            Log.i(TAG, "Sent cot: " + cot.toString());
        } catch (IOException e) {
            shutdown();
        }
    }

    private void initialiseDestAddress() {
        try {
            destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.TCP_ADDRESS));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error parsing destination address: " + prefs.getString(Key.TCP_ADDRESS, ""));
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
