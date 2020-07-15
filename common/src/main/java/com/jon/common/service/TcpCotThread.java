package com.jon.common.service;

import android.content.SharedPreferences;

import com.jon.common.cot.CursorOnTarget;
import com.jon.common.utils.Constants;
import com.jon.common.utils.DataFormat;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

class TcpCotThread extends CotThread {
    protected List<Socket> sockets = new ArrayList<>();
    protected List<CotStream> cotStreams = new ArrayList<>();
    protected boolean emulateMultipleUsers;

    TcpCotThread(SharedPreferences prefs) {
        super(prefs);
        dataFormat = DataFormat.XML; // regardless of what the preference is set as
        emulateMultipleUsers = PrefUtils.getBoolean(prefs, Key.EMULATE_MULTIPLE_USERS);
    }

    @Override
    void shutdown() {
        super.shutdown();
        try {
            for (CotStream cotStream : cotStreams)
                cotStream.stream.close();
            for (Socket socket : sockets)
                socket.close();
        } catch (IOException e) {
            /* ignore, we're shutting down anyway */
        }
        sockets.clear();
        cotStreams.clear();
    }

    @Override
    public void run() {
        try {
            super.run();
            initialiseDestAddress();
            openSockets();
            initialiseCotStreams();
            int bufferTimeMs = periodMilliseconds() / cotIcons.size();

            while (isRunning) {
                for (CotStream cotStream : cotStreams) {
                    if (!isRunning) break;
                    sendToDestination(cotStream);
                    bufferSleep(bufferTimeMs);
                }
                updateCotStreams();
            }
        } catch (Exception e) {
            /* We've encountered an unexpected exception, so close all sockets and pass the message back to our
            * thread exception handler */
            throw new RuntimeException(e.getMessage());
        } finally {
            shutdown();
        }
    }

    protected void sendToDestination(CotStream cotStream) throws IOException {
        try {
            final byte[] cotBytes = cotStream.cot.toBytes(dataFormat);
            cotStream.stream.write(cotBytes);
            Timber.i("Sent cot: %s", cotStream.cot.callsign);
        } catch (NullPointerException e) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown();
        }
    }

    protected void initialiseDestAddress() throws UnknownHostException {
        destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS));
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT);
    }

    protected void openSockets() throws Exception {
        final int numSockets = emulateMultipleUsers ? cotIcons.size() : 1;
        for (int i = 0; i < numSockets; i++) {
            Socket socket = new Socket();
            socket.connect(
                    new InetSocketAddress(destIp, destPort),
                    Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS
            );
            sockets.add(socket);
        }
    }

    private void initialiseCotStreams() throws IOException {
        for (int i = 0; i < cotIcons.size(); i++) {
            final CotStream cotStream = new CotStream();
            cotStream.cot = cotIcons.get(i);
            final int socketIndex = emulateMultipleUsers ? i : 0;
            cotStream.stream = sockets.get(socketIndex).getOutputStream();
            cotStreams.add(cotStream);
        }
    }

    private void updateCotStreams() {
        cotIcons = cotFactory.generate();
        for (int i = 0; i < cotStreams.size(); i++) {
            cotStreams.get(i).cot = cotIcons.get(i);
        }
    }

    /* Simple container class for a CoT icon and a corresponding output stream */
    protected class CotStream {
        CursorOnTarget cot;
        OutputStream stream;
    }
}
