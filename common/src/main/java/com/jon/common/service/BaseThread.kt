package com.jon.common.service

import android.content.SharedPreferences
import androidx.annotation.CallSuper
import com.jon.common.utils.DataFormat
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant
import java.net.InetAddress
import java.util.concurrent.TimeUnit

internal abstract class BaseThread(protected val prefs: SharedPreferences) : Thread() {

    private val lock = Any()
    private var bIsRunning = false

    protected var dataFormat = DataFormat.fromPrefs(prefs)
    protected var cotFactory = Variant.getCotFactory(prefs)
    protected var cotIcons = cotFactory.generate()

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
        }
    }

    open fun shutdown() {
        synchronized(lock) {
            bIsRunning = false
            cotFactory.clear()
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
        return TimeUnit.SECONDS.toMillis(
                PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD).toLong()
        )
    }

    companion object {
        fun fromPrefs(prefs: SharedPreferences): BaseThread {
            return when (Protocol.fromPrefs(prefs)) {
                Protocol.UDP -> UdpThread(prefs)
                Protocol.TCP -> TcpThread(prefs)
                Protocol.SSL -> SslThread(prefs)
            }
        }
    }
}
