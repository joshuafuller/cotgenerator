package com.jon.common.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

class NetworkHelper {
    fun getValidInterfaces(): List<NetworkInterface> {
        return NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .filter { it.isUp && interfaceHasIpv4Address(it) }
                .toList()
    }

    fun getAddressFromInterface(ni: NetworkInterface): InetAddress? {
        return ni.interfaceAddresses.map { it.address }.firstOrNull { it is Inet4Address }
    }

    private fun interfaceHasIpv4Address(ni: NetworkInterface): Boolean {
        return getAddressFromInterface(ni) != null
    }
}
