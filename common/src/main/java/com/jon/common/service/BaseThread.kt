package com.jon.common.service

import android.content.SharedPreferences
import androidx.annotation.CallSuper
import com.jon.common.cot.CursorOnTarget
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getIntFromPair
import com.jon.common.repositories.ISocketRepository
import com.jon.common.utils.DataFormat
import com.jon.common.utils.Protocol
import com.jon.common.utils.exhaustive
import java.net.InetAddress

abstract class BaseThread(protected val prefs: SharedPreferences) : Thread() {

    private val lock = Any()
    private var bIsRunning = false

    protected var dataFormat = DataFormat.fromPrefs(prefs)
    protected lateinit var cotFactory: CotFactory
    protected lateinit var socketRepository: ISocketRepository
    protected lateinit var cotIcons: List<CursorOnTarget>

    protected lateinit var destIp: InetAddress
    protected var destPort: Int = 0

    protected abstract fun initialiseDestAddress()
    protected abstract fun openSockets()

    open fun isRunning(): Boolean {
        synchronized(lock) {
            return bIsRunning
        }
    }

    @CallSuper
    override fun run() {
        synchronized(lock) {
            bIsRunning = true
            cotIcons = cotFactory.generate()
        }
    }

    open fun shutdown() {
        synchronized(lock) {
            bIsRunning = false
            interrupt()
        }
    }

    protected open fun bufferSleep(bufferTimeMs: Long) {
        try {
            sleep(bufferTimeMs)
        } catch (e: InterruptedException) {
            /* do nothing */
        }
    }

    protected open fun periodMilliseconds(): Long {
        return prefs.getIntFromPair(CommonPrefs.TRANSMISSION_PERIOD).toLong() * 1000
    }

    companion object {
        fun fromPrefs(
                prefs: SharedPreferences,
                socketRepository: ISocketRepository,
                cotFactory: CotFactory,
        ): BaseThread {
            return when (Protocol.fromPrefs(prefs)) {
                Protocol.UDP -> UdpThread(prefs)
                Protocol.TCP -> TcpThread(prefs)
                Protocol.SSL -> SslThread(prefs)
            }.exhaustive.also {
                it.cotFactory = cotFactory
                it.socketRepository = socketRepository
            }
        }
    }
}
