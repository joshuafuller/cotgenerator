package com.jon.cot.generator.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class NetworkHelper {
    private NetworkHelper() { }

    public static List<NetworkInterface> getValidInterfaces() throws SocketException {
        List<NetworkInterface> interfaces = new ArrayList<>();
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isUp() && interfaceHasIpv4Address(ni)) {
                interfaces.add(ni);
                Timber.d("%s is up", ni.getName());
            }
        }
        return interfaces;
    }

    public static InetAddress getAddressFromInterface(NetworkInterface ni) {
        for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
            if (interfaceAddress.getAddress() instanceof Inet4Address) {
                return interfaceAddress.getAddress();
            }
        }
        return null;
    }

    private static boolean interfaceHasIpv4Address(NetworkInterface ni) {
        return getAddressFromInterface(ni) != null;
    }
}