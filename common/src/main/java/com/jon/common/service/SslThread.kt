package com.jon.common.service

import android.content.SharedPreferences
import android.os.AsyncTask
import com.jon.common.cot.CursorOnTarget
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.IPresetRepository
import com.jon.common.utils.Constants
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.net.SocketException
import java.security.*
import java.security.cert.CertificateException
import java.util.concurrent.ExecutionException
import javax.net.ssl.*

internal class SslThread(
        prefs: SharedPreferences,
        private val presetRepository: IPresetRepository
) : TcpThread(prefs) {

    override fun shutdown() {
        super.shutdown()
        closeFromMainThread(outputStream)
        closeFromMainThread(socket)
    }

    @Throws(IOException::class)
    override fun sendToDestination(cot: CursorOnTarget) {
        try {
            outputStream.write(cot.toBytes(dataFormat))
            Timber.i("Sent cot: %s", cot.callsign)
        } catch (e: NullPointerException) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown()
        } catch (e: SocketException) {
            shutdown()
        }
    }

    @Throws(Exception::class)
    override fun openSockets() {
        val preset = buildPreset()
        val certStore = loadKeyStore(
                preset.clientCert,
                preset.clientCertPassword!!.toCharArray()
        )
        val trustStore = loadKeyStore(
                preset.trustStore,
                preset.trustStorePassword!!.toCharArray()
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(
                getKeyManagers(certStore, preset.clientCertPassword!!.toCharArray()),
                getTrustManagers(trustStore),
                SecureRandom()
        )
        socket = (sslContext.socketFactory.createSocket(destIp, destPort) as SSLSocket).also {
            it.enabledProtocols = arrayOf("TLSv1.1", "TLSv1.2")
            it.soTimeout = Constants.TCP_SOCKET_TIMEOUT_MS
            outputStream = it.outputStream
        }
    }

    private fun buildPreset(): OutputPreset {
        /* This contains only the basic values: protocol, alias, address and port. We now need to query the
         * database to grab SSL cert information, then return this upgraded OutputPreset object if successful. */
        val basicPreset = OutputPreset.fromString(prefs.getStringFromPair(CommonPrefs.SSL_PRESETS))
        val sslPreset = presetRepository.getPreset(basicPreset!!.protocol, basicPreset.address, basicPreset.port)
        return if (sslPreset == null) {
            /* If there is no database entry corresponding to the above protocol/address/port combo,
             * we treat this as a default. So grab the values from default certs */
            val sslDefaults = presetRepository.defaultsByProtocol(basicPreset.protocol)
            if (sslDefaults.isNotEmpty()) {
                sslDefaults[0] // Discord TAK Server
            } else {
                throw RuntimeException("Check the length of the SSL Presets list, YOU IDIOT")
            }
        } else {
            /* Valid cert info fetched from the database, so return it. */
            sslPreset
        }
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun loadKeyStore(bytes: ByteArray?, password: CharArray): KeyStore {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(ByteArrayInputStream(bytes), password)
        return keyStore
    }

    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class)
    private fun getTrustManagers(trustStore: KeyStore): Array<TrustManager?>? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)
        return trustManagerFactory.trustManagers
    }

    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class, UnrecoverableKeyException::class)
    private fun getKeyManagers(certStore: KeyStore, password: CharArray): Array<KeyManager?>? {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(certStore, password)
        return keyManagerFactory.keyManagers
    }

    /* This workaround is only necessary because I was getting NetworkOnMainThreadExceptions when
     * closing the SSLSocket via shutdown() from the UI thread. This wasn't an issue with TCP/UDP
     * so this is just a patch to deal with it for now. */
    private fun closeFromMainThread(closeable: Closeable?) {
        try {
            CloseSocketTask().execute(closeable).get()
        } catch (e: ExecutionException) {
            /* Ignore */
        } catch (e: InterruptedException) {
            /* Ignore */
        }
    }

    private class CloseSocketTask : AsyncTask<Closeable?, Void, Void?>() {
        override fun doInBackground(vararg closeables: Closeable?): Void? {
            for (closeable in closeables) {
                try {
                    closeable?.close()
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
            return null
        }
    }
}