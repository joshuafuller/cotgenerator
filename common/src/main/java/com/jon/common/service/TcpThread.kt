package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.cot.CursorOnTarget
import com.jon.common.utils.Constants
import com.jon.common.utils.DataFormat
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal open class TcpThread(prefs: SharedPreferences) : BaseThread(prefs) {
    protected var sockets: MutableList<Socket> = ArrayList()
    private var cotStreams: MutableList<CotStream> = ArrayList()
    protected var emulateMultipleUsers = PrefUtils.getBoolean(prefs, Key.EMULATE_MULTIPLE_USERS)

    init {
        dataFormat = DataFormat.XML // regardless of what the preference is set as
    }

    override fun shutdown() {
        super.shutdown()
        try {
            Timber.d("Shutting down output streams")
            cotStreams.forEach { it.stream.close() }
            Timber.d("Shutting down sockets")
            sockets.forEach { it.close() }
        } catch (e: Exception) {
            Timber.w(e)
        }
        sockets.clear()
        cotStreams.clear()
    }

    override fun run() {
        try {
            super.run()
            initialiseDestAddress()
            openSockets()
            initialiseCotStreams()
            val bufferTimeMs = periodMilliseconds() / cotIcons.size.toLong()
            while (isRunning()) {
                for (cotStream in cotStreams) {
                    if (!isRunning()) break
                    sendToDestination(cotStream)
                    bufferSleep(bufferTimeMs)
                }
                updateCotStreams()
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

    private fun sendToDestination(cotStream: CotStream) {
        try {
            val cotBytes = cotStream.cot.toBytes(dataFormat)
            cotStream.stream.write(cotBytes)
            Timber.i("Sent cot: %s at %f:%f", cotStream.cot.callsign, cotStream.cot.lat, cotStream.cot.lon)
        } catch (e: NullPointerException) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown()
        }
    }

    override fun initialiseDestAddress() {
        Timber.d("Initialising destination address/port")
        destIp = InetAddress.getByName(PrefUtils.getString(prefs, Key.DEST_ADDRESS))
        destPort = PrefUtils.parseInt(prefs, Key.DEST_PORT)
    }

    override fun openSockets() {
        sockets.clear()
        Timber.d("Opening sockets")
        val numSockets = if (emulateMultipleUsers) cotIcons.size else 1

        /* Thread-safe list */
        val synchronisedSockets = Collections.synchronizedList(ArrayList<Socket>())
        val executorService = Executors.newCachedThreadPool()
        for (i in 0 until numSockets) {
            /* Execute the socket-building code on a separate thread per socket, since it takes a while with lots of sockets */
            executorService.execute {
                try {
                    buildSocket(synchronisedSockets)
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
        collectSynchronisedSockets(executorService, synchronisedSockets)
    }

    @Suppress("ControlFlowWithEmptyBody")
    protected fun collectSynchronisedSockets(executorService: ExecutorService, synchronisedSockets: MutableList<Socket>) {
        try {
            /* Block the thread until all sockets are built */
            executorService.shutdown()
            while (!executorService.awaitTermination(1, TimeUnit.MINUTES));
            sockets = synchronisedSockets
            Timber.d("All sockets open!")
        } catch (e: InterruptedException) {
            /* Thrown if we stop the service whilst sockets are being built */
            executorService.shutdownNow()
            Timber.w(e)
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS));
            synchronisedSockets.forEach { it.close() }
            shutdown()
        }
    }

    private fun buildSocket(synchronisedSockets: MutableList<Socket>) {
        Timber.d("Opening socket")
        val socket = Socket()
        socket.connect(
                InetSocketAddress(destIp, destPort),
                Constants.TCP_SOCKET_TIMEOUT_MS
        )
        Timber.d("Opened socket on port %d", socket.localPort)
        synchronisedSockets.add(socket)
    }

    private fun initialiseCotStreams() {
        Timber.d("Initialising output streams")
        cotStreams.clear()
        for (i in cotIcons.indices) {
            val socketIndex = if (emulateMultipleUsers) i else 0
            try {
                cotStreams.add(CotStream(
                        cot = cotIcons[i],
                        stream = sockets[socketIndex].getOutputStream()
                ))
            } catch (e: SocketException) {
                /* Thrown if the "sockets" instance has been cleared/shutdown before we get to this point */
                shutdown()
                break
            } catch (e: IndexOutOfBoundsException) {
                shutdown()
                break
            }
        }
    }

    private fun updateCotStreams() {
        Timber.d("Updating output streams")
        cotIcons = cotFactory.generate()
        for (i in cotStreams.indices) {
            cotStreams[i].cot = cotIcons[i]
        }
    }

    /* Simple container class for a CoT icon and a corresponding output stream */
    protected data class CotStream( var cot: CursorOnTarget, var stream: OutputStream)
}
