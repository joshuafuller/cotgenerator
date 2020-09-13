package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.PresetRepository
import com.jon.common.utils.Constants
import com.jon.common.utils.Key
import com.jon.common.utils.PrefUtils.getString
import com.jon.common.utils.Protocol
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.Socket
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import java.util.concurrent.Executors
import javax.net.SocketFactory
import javax.net.ssl.*

internal class SslThread(prefs: SharedPreferences) : TcpThread(prefs) {

    override fun openSockets() {
        sockets.clear()
        val socketFactory = buildSocketFactory()
        Timber.d("Opening sockets")
        val numSockets = if (emulateMultipleUsers) cotIcons.size else 1
        val executorService = Executors.newCachedThreadPool()

        /* Thread-safe list */
        val synchronisedSockets = Collections.synchronizedList(ArrayList<Socket>())
        for (i in 0 until numSockets) {
            /* Execute the socket-building code on a separate thread per socket, since it takes a while with lots of sockets */
            executorService.execute { buildSocket(socketFactory, synchronisedSockets) }
        }
        collectSynchronisedSockets(executorService, synchronisedSockets)
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class, UnrecoverableKeyException::class, KeyManagementException::class)
    private fun buildSocketFactory(): SocketFactory {
        Timber.d("Building SSL context")
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
        return sslContext.socketFactory
    }

    private fun buildPreset(): OutputPreset {
        /* This contains only the basic values: protocol, alias, address and port. We now need to query the
         * database to grab SSL cert information, then return this upgraded OutputPreset object if successful. */
        val prefString = getString(prefs, Key.SSL_PRESETS)
        val basicPreset = OutputPreset.fromString(prefString)
                ?: throw RuntimeException("Couldn't parse a preset from SSL preset string: '$prefString'")
        val repository = PresetRepository.getInstance()
        val sslPreset = repository.getPreset(Protocol.SSL, basicPreset.address, basicPreset.port)
        return if (sslPreset == null) {
            /* If there is no database entry corresponding to the above protocol/address/port combo,
             * we treat this as a default. So grab the values from default certs */
            val sslDefaults = repository.defaultsByProtocol(Protocol.SSL)
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
    private fun getTrustManagers(trustStore: KeyStore): Array<TrustManager> {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)
        return trustManagerFactory.trustManagers
    }

    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class, UnrecoverableKeyException::class)
    private fun getKeyManagers(certStore: KeyStore, password: CharArray): Array<KeyManager> {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(certStore, password)
        return keyManagerFactory.keyManagers
    }

    private fun buildSocket(socketFactory: SocketFactory, synchronisedSockets: MutableList<Socket>) {
        try {
            Timber.d("Opening socket")
            val socket: Socket = socketFactory.createSocket(destIp, destPort)
            socket.soTimeout = Constants.TCP_SOCKET_TIMEOUT_MS
            synchronisedSockets.add(socket)
            Timber.d("Opened socket on port %d", socket.localPort)
        } catch (e: Exception) {
            Timber.e(e)
            throw RuntimeException(e)
        }
    }
}