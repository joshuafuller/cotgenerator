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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            Timber.d("Shutting down output streams");
            for (CotStream cotStream : cotStreams)
                cotStream.stream.close();
            Timber.d("Shutting down sockets");
            for (Socket socket : sockets)
                socket.close();
        } catch (Exception e) {
            Timber.w(e);
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
            Timber.e(e);
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

    @Override
    protected void initialiseDestAddress() throws UnknownHostException {
        Timber.d("Initialising destination address/port");
        destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS));
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT);
    }

    @Override
    protected void openSockets() throws Exception {
        sockets.clear();
        Timber.d("Opening sockets");
        final int numSockets = emulateMultipleUsers ? cotIcons.size() : 1;

        /* Thread-safe list */
        final List<Socket> synchronisedSockets = Collections.synchronizedList(new ArrayList<>());
        final ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < numSockets; i++) {
            /* Execute the socket-building code on a separate thread per socket, since it takes a while with lots of sockets */
            executorService.execute(() -> {
                try {
                    buildSocket(synchronisedSockets);
                } catch (IOException e) {
                    Timber.e(e);
                }
            });
        }
        collectSynchronisedSockets(executorService, synchronisedSockets);
    }

    protected void collectSynchronisedSockets(ExecutorService executorService, List<Socket> synchronisedSockets)
            throws InterruptedException, IOException {
        try {
            /* Block the thread until all sockets are built */
            executorService.shutdown();
            while (!executorService.awaitTermination(1, TimeUnit.MINUTES));
            sockets = synchronisedSockets;
            Timber.d("All sockets open!");
        } catch (InterruptedException e) {
            /* Thrown if we stop the service whilst sockets are being built */
            executorService.shutdownNow();
            Timber.w(e);
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS));
            for (Socket socket : synchronisedSockets) socket.close();
            shutdown();
        }
    }

    private void buildSocket(List<Socket> synchronisedSockets) throws IOException {
        Timber.d("Opening socket");
        Socket socket = new Socket();
        socket.connect(
                new InetSocketAddress(destIp, destPort),
                Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS
        );
        Timber.d("Opened socket on port %d", socket.getLocalPort());
        synchronisedSockets.add(socket);
    }

    private void initialiseCotStreams() throws IOException {
        Timber.d("Initialising output streams");
        cotStreams.clear();
        for (int i = 0; i < cotIcons.size(); i++) {
            final CotStream cotStream = new CotStream();
            cotStream.cot = cotIcons.get(i);
            final int socketIndex = emulateMultipleUsers ? i : 0;
            try {
                cotStream.stream = sockets.get(socketIndex).getOutputStream();
                cotStreams.add(cotStream);
            } catch (SocketException | IndexOutOfBoundsException e) {
                /* Thrown if the "sockets" instance has been cleared/shutdown before we get to this point */
                shutdown();
                break;
            }
        }
    }

    private void updateCotStreams() {
        Timber.d("Updating output streams");
        cotIcons = cotFactory.generate();
        for (int i = 0; i < cotStreams.size(); i++) {
            cotStreams.get(i).cot = cotIcons.get(i);
        }
    }

    /* Simple container class for a CoT icon and a corresponding output stream */
    protected static class CotStream {
        CursorOnTarget cot;
        OutputStream stream;
    }
}
