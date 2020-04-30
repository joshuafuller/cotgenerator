package com.jon.cotgenerator.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.utils.PrefUtils;
import com.jon.cotgenerator.utils.Constants;
import com.jon.cotgenerator.utils.Key;

import java.io.*;
import java.net.*;
import java.util.*;

public class CotThread extends Thread {
    private static final String TAG = CotThread.class.getSimpleName();
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 1 / DEG_TO_RAD;

    private class Point {
        private int id;
        double lat;
        double lon;
        Point(final double la, final double lo) {
            id = GenerateInt.next();
            lat = la;
            lon = lo;
        }
        Point() {
            id = GenerateInt.next();
        }
    }

    private static class GenerateInt {
        private static int i = 3;
        private GenerateInt() { }
        static int next() {
            return i++;
        }
    }

    private SharedPreferences mPrefs;
    private volatile DatagramSocket mSendSocket = null;
    private volatile boolean mIsRunning = false;

    private InetAddress mIp;
    private int mPort;
    boolean mTransmitUdp;

    CotThread(SharedPreferences prefs) {
        mPrefs = prefs;
        mTransmitUdp = mPrefs.getString(Key.TRANSMISSION_PROTOCOL, "UDP").equals("UDP");
        String ipKey = mTransmitUdp ? Key.UDP_IP : Key.TCP_IP;
        String portKey = mTransmitUdp ? Key.UDP_PORT : Key.TCP_PORT;

        try {
            mPort = Integer.parseInt(mPrefs.getString(portKey, ""));
            mIp = InetAddress.getByName(mPrefs.getString(ipKey, ""));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Invalid IP address '" + mPrefs.getString(ipKey, "") + "'");
        }
    }

    boolean isRunning() {
        return mIsRunning;
    }

    void shutdown() {
        mIsRunning = false;
        if (mSendSocket != null) {
            mSendSocket.close();
            mSendSocket = null;
        }
        interrupt();
    }

    @Override
    public void run() {
        Log.d(TAG, "============= OPENING THREAD =============");
        mIsRunning = true;
        final boolean sendGps = PrefUtils.getString(mPrefs, Key.TRANSMITTED_DATA).equals("GPS Position");
        final int movementRadius = PrefUtils.getInt(mPrefs, Key.MOVEMENT_RADIUS);

        Map<Point, CursorOnTarget> allCot = buildCot();
        Map<String, DatagramSocket> allSockets = buildSockets();

        while (mIsRunning) {
            final long start = System.currentTimeMillis();

            /* Update the generated tracks */
            Log.i(TAG, "Updating " + allSockets.size() + " sockets");
            for (Map.Entry socketEntry : allSockets.entrySet()) {
                Log.i(TAG, "Transmitting over " + socketEntry.getKey());
                final DatagramSocket socket = (DatagramSocket)socketEntry.getValue();
                Log.i(TAG, "Transmitting " + allCot.size() + " icons");
                for (Map.Entry cotEntry : allCot.entrySet()) {
                    CursorOnTarget cot = (CursorOnTarget)cotEntry.getValue();
                    try {
                        byte[] buf = cot.toBytes();
                        socket.send(new DatagramPacket(buf, buf.length, mIp, mPort));
                        Log.i(TAG, "Sent " + cot.callsign);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed sending " + cot.callsign + " over " + cotEntry.getKey());
                    }

                    /* update the cot's position and overwrite it in the array */
                    final Point centrePoint = (Point)cotEntry.getKey();
                    allCot.put(centrePoint, updateCotPosition(cot, centrePoint, movementRadius));
                }
            }

            if (sendGps) {

            } else {
                if (mTransmitUdp) {

                } else {

                }
            }

            /* Pause the thread until the next generation time */
            try {
                final int periodSeconds = PrefUtils.getInt(mPrefs, Key.TRANSMISSION_PERIOD);
                final long dt = (periodSeconds * 1000) - (System.currentTimeMillis() - start);
                if (dt > 0) {
                    Log.i(TAG, "Sleeping for " + dt + " milliseconds");
                    Thread.sleep(dt);
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "The service has been interrupted from UI");
            }
        }

        mIsRunning = false;
        Log.d(TAG, "============= CLOSING THREAD =============");
    }

    private CursorOnTarget updateCotPosition(final CursorOnTarget cot, final Point centre, final int movementRadius) {
        final Random random = new Random();
        if (movementRadius == 0) {
            /* No movement, so no work to do */
            return cot;
        }

        final double dlat = (double)movementRadius / Constants.EARTH_RADIUS;
        final double dlon = dlat / Math.cos(centre.lat);
        PrimitiveIterator.OfDouble latItr = random.doubles(centre.lat-dlat, centre.lat+dlat).iterator();
        PrimitiveIterator.OfDouble lonItr = random.doubles(centre.lon-dlon, centre.lon+dlon).iterator();
        final Point p = generatePoint(latItr, lonItr, centre, movementRadius);
        cot.lat = p.lat * RAD_TO_DEG;
        cot.lon = p.lon * RAD_TO_DEG;
        return cot;
    }

    private Point generatePoint(final PrimitiveIterator.OfDouble lat, final PrimitiveIterator.OfDouble lon, final Point centre, final double radius) {
        final Point point = new Point(lat.next(), lon.next());
        if (arcdistance(centre, point) > radius) {
            return generatePoint(lat, lon, centre, radius);
        } else {
            return point;
        }
    }

    private Map<Point, CursorOnTarget> buildCot() {
        Map<Point, CursorOnTarget> icons = new HashMap<>();
        final int nCot = PrefUtils.getInt(mPrefs, Key.ICON_COUNT);
        final String prefix = PrefUtils.getString(mPrefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        final long staleTimer = 1000 * 60 * PrefUtils.getInt(mPrefs, Key.STALE_TIMER);
        final double radius = PrefUtils.getDouble(mPrefs, Key.RADIAL_DISTRIBUTION);
        final Point centre = new Point(
                PrefUtils.getDouble(mPrefs, Key.CENTRE_LATITUDE) * DEG_TO_RAD,
                PrefUtils.getDouble(mPrefs, Key.CENTRE_LONGITUDE) * DEG_TO_RAD
        );
        final String team = PrefUtils.getString(mPrefs, Key.TEAM_COLOUR);
        final Random random = new Random();
        final double dlat = radius / Constants.EARTH_RADIUS;
        final double dlon = Math.abs(dlat / Math.cos(centre.lat));
        PrimitiveIterator.OfDouble latItr = random.doubles(centre.lat-dlat, centre.lat+dlat).iterator();
        PrimitiveIterator.OfDouble lonItr = random.doubles(centre.lon-dlon, centre.lon+dlon).iterator();

        for (int i = 0; i < nCot; i++) {
            CursorOnTarget cot = new CursorOnTarget();
            cot.uid = String.format(Locale.ENGLISH, "%s_%06d", prefix, i);
            cot.callsign = String.format(Locale.ENGLISH, "%s_%d", prefix, i);
            cot.time = now;
            cot.start = now;
            cot.setStaleDiff(staleTimer);
            cot.team = team;
            final Point point = generatePoint(latItr, lonItr, centre, radius);
            cot.lat = point.lat * RAD_TO_DEG;
            cot.lon = point.lon * RAD_TO_DEG;
            icons.put(point, cot);
        }
        return icons;
    }

    private static double arcdistance(final Point p1, final Point p2) {
        final double phi1 = p1.lat;
        final double phi2 = p2.lat;
        final double dphi = phi2 - phi1;
        final double dtheta = (p2.lon - p1.lon);

        /* I can feel myself getting sweaty just looking at this */
        final double a = Math.sin(dphi/2) * Math.sin(dphi/2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dtheta/2) * Math.sin(dtheta/2);
        return 2 * Constants.EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private Map<String, DatagramSocket> buildSockets() {
        Map<String, DatagramSocket> socketMap = new HashMap<>();
        final boolean isMulticast = mIp.isMulticastAddress();
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface ni = interfaces.nextElement();
                final String name = ni.getName();
                try {
                    final boolean isWanted = name.contains("eth") || name.contains("wlan");
                    if (ni.isUp() && isWanted) {
                        if (mTransmitUdp) {
                            if (isMulticast) {
                                MulticastSocket socket = new MulticastSocket();
                                socket.joinGroup(new InetSocketAddress(mIp, mPort), ni);
                                socket.setNetworkInterface(ni);
                                socketMap.put(name, socket);
                            } else {
                                socketMap.put(name, new DatagramSocket());
                            }
                        } else {
                            Log.e(TAG, "Need to implement TCP!");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed querying interface " + name);
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Failed getting the list of network interfaces");
        }
        return socketMap;
    }
}
