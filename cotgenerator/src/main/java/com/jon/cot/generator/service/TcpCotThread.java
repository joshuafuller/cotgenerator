package com.jon.cot.generator.service;

import android.content.SharedPreferences;

import com.jon.cot.generator.cot.CursorOnTarget;
import com.jon.cot.generator.utils.Constants;
import com.jon.cot.generator.utils.DataFormat;
import com.jon.cot.generator.utils.Key;
import com.jon.cot.generator.utils.PrefUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import timber.log.Timber;

class TcpCotThread extends CotThread {
    private Socket socket;
    private OutputStream outputStream;

    TcpCotThread(SharedPreferences prefs) {
        super(prefs);
        dataFormat = DataFormat.XML; // regardless of what the preference is set as
    }

    @Override
    void shutdown() {
        super.shutdown();
        if (socket != null) {
            try {
                outputStream.close();
                socket.close();
            } catch (Exception e) {
                /* ignore, we're shutting down anyway */
            }
            outputStream = null;
            socket = null;
        }
    }

    @Override
    public void run() {
        try {
            super.run();
            initialiseDestAddress();
            openSocket();
            int bufferTimeMs = periodMilliseconds() / cotIcons.size();

            while (isRunning) {
                for (CursorOnTarget cot : cotIcons) {
                    if (!isRunning) break;
                    sendToDestination(cot);
                    bufferSleep(bufferTimeMs);
                }
                cotIcons = cotGenerator.generate();
            }
            shutdown();
        } catch (Exception e) {
            shutdown();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void sendToDestination(CursorOnTarget cot) throws IOException {
        try {
            outputStream.write(cot.toBytes(dataFormat));
            Timber.i("Sent cot: %s", cot.callsign);
        } catch (NullPointerException e) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown();
        }
    }

    protected void initialiseDestAddress() throws UnknownHostException {
        destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS));
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT);
    }

    protected void openSocket() throws Exception {
        socket = new Socket();
        socket.connect(
                new InetSocketAddress(destIp, destPort),
                Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS
        );
        outputStream = socket.getOutputStream();
    }
}
