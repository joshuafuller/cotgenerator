package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.NetworkHelper;
import com.jon.cotgenerator.utils.PrefUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

class UdpCotThread extends CotThread {
    private List<DatagramSocket> sockets = new ArrayList<>();
    private List<String> interfaceNames = new ArrayList<>();

    UdpCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    void shutdown() {
        super.shutdown();
        for (DatagramSocket socket : sockets) {
            socket.close();
        }
        sockets.clear();
        interfaceNames.clear();
    }

    @Override
    public void run() {
        super.run();
        initialiseDestAddress();
        openSockets();
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

    protected void initialiseDestAddress() {
        try {
            destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS));
        } catch (UnknownHostException e) {
            Timber.e("Error parsing destination address: %s", PrefUtils.getString(prefs, Key.DEST_ADDRESS));
            shutdown();
        } catch (Exception e) {
            Timber.e(e);
        }
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT);
    }

    protected void openSockets() {
        try {
            if (destIp.isMulticastAddress()) {
                final List<NetworkInterface> interfaces = NetworkHelper.getValidInterfaces();
                for (NetworkInterface ni : interfaces) {
                    InetAddress address = NetworkHelper.getAddressFromInterface(ni);
                    if (address != null) {
                        Timber.i("Interface %s is valid with address %s", ni.getName(), address.getHostAddress());
                        MulticastSocket socket = new MulticastSocket();
                        socket.setNetworkInterface(ni);
                        socket.setLoopbackMode(false);
                        sockets.add(socket);
                        interfaceNames.add(ni.getName());
                    }
                }
            } else {
                sockets.add(new DatagramSocket());
                interfaceNames.add("all interfaces");
            }
        } catch (IOException e) {
            Timber.e("Error when building transmit UDP socket");
            Timber.e(e);
            shutdown();
        }
    }

    @Override
    protected void sendToDestination(CursorOnTarget cot) {
        try {
            final byte[] buf = cot.toBytes(dataFormat);
            for (int i = 0; i < sockets.size(); i++) {
                sockets.get(i).send(new DatagramPacket(buf, buf.length, destIp, destPort));
                Timber.i("Sent %s over %s: %s", cot.callsign, interfaceNames.get(i), new String(buf));
            }
        } catch (IOException e) {
            Timber.w(e);
            shutdown();
        } catch (NullPointerException e) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown();
        }
    }
}
