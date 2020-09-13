package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.utils.Key
import com.jon.common.utils.NetworkHelper
import com.jon.common.utils.PrefUtils
import timber.log.Timber
import java.io.IOException
import java.net.*
import java.util.*

internal class UdpThread(prefs: SharedPreferences) : BaseThread(prefs) {
    private val sockets: MutableList<DatagramSocket> = ArrayList()

    override fun shutdown() {
        super.shutdown()
        sockets.forEach { it.close() }
        sockets.clear()
    }

    override fun run() {
        try {
            super.run()
            initialiseDestAddress()
            openSockets()
            val bufferTimeMs = periodMilliseconds() / cotIcons.size
            while (isRunning()) {
                for (cot in cotIcons) {
                    if (!isRunning()) break
                    sendToDestination(cot)
                    bufferSleep(bufferTimeMs)
                }
                cotIcons = cotFactory.generate()
            }
        } catch (e: Exception) {
            /* We've encountered an unexpected exception, so close all sockets and pass the message back to our
             * thread exception handler */
            Timber.e(e)
            throw RuntimeException(e.message)
        } finally {
            shutdown()
        }
    }

    @Throws(UnknownHostException::class)
    override fun initialiseDestAddress() {
        destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS))
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT)
    }

    @Throws(IOException::class)
    override fun openSockets() {
        val networkHelper = NetworkHelper()
        if (destIp.isMulticastAddress) {
            networkHelper.getValidInterfaces().forEach { ni ->
                networkHelper.getAddressFromInterface(ni)?.let {
                    val socket = MulticastSocket()
                    socket.networkInterface = ni
                    socket.loopbackMode = false
                    sockets.add(socket)
                }
            }
        } else {
            sockets.add(DatagramSocket())
        }
    }

    @Throws(IOException::class)
    private fun sendToDestination(cot: CursorOnTarget) {
        try {
            val buf = cot.toBytes(dataFormat)
            sockets.forEach {
                it.send(DatagramPacket(buf, buf.size, destIp, destPort))
                Timber.i("Sent %s over %s", cot.callsign, it.inetAddress.hostAddress)
            }
        } catch (e: NullPointerException) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown()
        }
    }
}
