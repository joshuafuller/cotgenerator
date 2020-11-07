package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.prefs.CommonPrefs
import com.jon.common.utils.NetworkHelper
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
                if (!isRunning()) break
                cotIcons = cotFactory.generate()
            }
        } catch (t: Throwable) {
            /* We've encountered an unexpected exception, so close all sockets and pass the message back to our
             * thread exception handler */
            Timber.e(t)
            throw RuntimeException(t.message)
        } finally {
            shutdown()
        }
    }

    @Throws(UnknownHostException::class)
    override fun initialiseDestAddress() {
        destIp = InetAddress.getByName(prefs.getString(CommonPrefs.DEST_ADDRESS, ""))
        destPort = prefs.getString(CommonPrefs.DEST_PORT, "")!!.toInt()
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
            }
            Timber.i("Sent %s over %d sockets", cot.callsign, sockets.size)
        } catch (e: NullPointerException) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown()
        }
    }
}
