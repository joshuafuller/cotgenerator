package com.jon.common.service;

import android.content.SharedPreferences;

import com.jon.common.presets.OutputPreset;
import com.jon.common.presets.PresetRepository;
import com.jon.common.utils.Constants;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;
import com.jon.common.utils.Protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import timber.log.Timber;

public class SslCotThread extends TcpCotThread {
    SslCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    protected void openSockets() throws Exception {
        sockets.clear();
        final SocketFactory socketFactory = buildSocketFactory();

        Timber.d("Opening sockets");
        final int numSockets = emulateMultipleUsers ? cotIcons.size() : 1;
        final ExecutorService executorService = Executors.newCachedThreadPool();

        /* Thread-safe list */
        final List<Socket> synchronisedSockets = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < numSockets; i++) {
            /* Execute the socket-building code on a separate thread per socket, since it takes a while with lots of sockets */
            executorService.execute(() -> buildSocket(socketFactory, synchronisedSockets));
        }
        collectSynchronisedSockets(executorService, synchronisedSockets);
    }

    private SocketFactory buildSocketFactory()
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException, KeyManagementException {
        Timber.d("Building SSL context");
        OutputPreset preset = buildPreset();
        KeyStore certStore = loadKeyStore(
                preset.clientCert,
                preset.clientCertPassword.toCharArray()
        );
        KeyStore trustStore = loadKeyStore(
                preset.trustStore,
                preset.trustStorePassword.toCharArray()
        );
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(
                getKeyManagers(certStore, preset.clientCertPassword.toCharArray()),
                getTrustManagers(trustStore),
                new SecureRandom()
        );
        return sslContext.getSocketFactory();
    }

    private OutputPreset buildPreset() {
        /* This contains only the basic values: protocol, alias, address and port. We now need to query the
         * database to grab SSL cert information, then return this upgraded OutputPreset object if successful. */
        final String prefString = PrefUtils.getString(prefs, Key.SSL_PRESETS);
        OutputPreset basicPreset = OutputPreset.fromString(prefString);
        if (basicPreset == null) {
            throw new RuntimeException("Couldn't parse a preset from SSL preset string: '" + prefString + "'");
        }
        PresetRepository repository = PresetRepository.getInstance();
        OutputPreset sslPreset = repository.getPreset(Protocol.SSL, basicPreset.address, basicPreset.port);
        if (sslPreset == null) {
            /* If there is no database entry corresponding to the above protocol/address/port combo,
             * we treat this as a default. So grab the values from default certs */
            List<OutputPreset> sslDefaults = repository.defaultsByProtocol(Protocol.SSL);
            if (sslDefaults.size() >= 1) {
                return sslDefaults.get(0); // Discord TAK Server
            } else {
                throw new RuntimeException("Check the length of the SSL Presets list, YOU IDIOT");
            }
        } else {
            /* Valid cert info fetched from the database, so return it. */
            return sslPreset;
        }
    }

    private KeyStore loadKeyStore(byte[] bytes, char[] password)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(bytes), password);
        return keyStore;
    }

    private TrustManager[] getTrustManagers(KeyStore trustStore)
            throws NoSuchAlgorithmException, KeyStoreException{
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private KeyManager[] getKeyManagers(KeyStore certStore, char[] password)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(certStore, password);
        return keyManagerFactory.getKeyManagers();
    }

    private void buildSocket(final SocketFactory socketFactory, final List<Socket> synchronisedSockets) {
        try {
            Timber.d("Opening socket");
            final Socket socket = socketFactory.createSocket(destIp, destPort);
            socket.setSoTimeout(Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS);
            synchronisedSockets.add(socket);
            Timber.d("Opened socket on port %d", socket.getLocalPort());
        } catch (Exception e) {
            Timber.e(e);
            throw new RuntimeException(e);
        }
    }
}
