package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.prefs.CommonPrefs
import com.jon.common.utils.Constants
import com.jon.common.utils.DataFormat
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.*


internal open class TcpThread(prefs: SharedPreferences) : BaseThread(prefs) {
    protected var sockets: MutableList<Socket> = ArrayList()

    init {
        dataFormat = DataFormat.XML // regardless of what the preference is set as
    }

    protected var socket: Socket? = null
    protected var outputStream: OutputStream? = null

    override fun shutdown() {
        super.shutdown()
        if (socket != null) {
            try {
                outputStream!!.close()
                socket!!.close()
            } catch (e: Exception) {
                /* ignore, we're shutting down anyway */
            }
            outputStream = null
            socket = null
        }
    }

    override fun run() {
        try {
            super.run()
            initialiseDestAddress()
            openSockets()
            val bufferTimeMs = (periodMilliseconds() / cotIcons.size).toInt()
            while (isRunning()) {
                for (cot in cotIcons) {
                    if (!isRunning()) break
                    sendToDestination(cot)
                    bufferSleep(bufferTimeMs.toLong())
                }
                cotIcons = cotFactory.generate()
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        } finally {
            shutdown()
        }
    }

    @Throws(IOException::class)
    protected open fun sendToDestination(cot: CursorOnTarget) {
        try {
            outputStream!!.write(cot.toBytes(dataFormat))
            Timber.i("Sent cot: %s", cot.callsign)
        } catch (e: NullPointerException) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown()
        }
    }

    @Throws(UnknownHostException::class)
    override fun initialiseDestAddress() {
        destIp = InetAddress.getByName(prefs.getString(CommonPrefs.DEST_ADDRESS, ""))
        destPort = prefs.getString(CommonPrefs.DEST_PORT, "")!!.toInt()
    }

    @Throws(Exception::class)
    override fun openSockets() {
        socket = Socket().also {
            it.connect(
                    InetSocketAddress(destIp, destPort),
                    Constants.TCP_SOCKET_TIMEOUT_MS
            )
            outputStream = it.getOutputStream()
        }
    }
}
