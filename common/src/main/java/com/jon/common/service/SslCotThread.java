package com.jon.common.service;

import android.content.SharedPreferences;

import com.jon.common.presets.OutputPreset;
import com.jon.common.presets.PresetRepository;
import com.jon.common.utils.Constants;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SslCotThread extends TcpCotThread {
    SslCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    protected void openSockets() throws Exception {
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
        final SocketFactory socketFactory = sslContext.getSocketFactory();

        final int numSockets = emulateMultipleUsers ? cotIcons.size() : 1;
        for (int i = 0; i < numSockets; i++) {
            final Socket socket = socketFactory.createSocket(destIp, destPort);
            socket.setSoTimeout(Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS);
            sockets.add(socket);
        }
    }

    private OutputPreset buildPreset() {
        /* This contains only the basic values: protocol, alias, address and port. We now need to query the
         * database to grab SSL cert information, then return this upgraded OutputPreset object if successful. */
        OutputPreset basicPreset = OutputPreset.fromString(PrefUtils.getString(prefs, Key.SSL_PRESETS));
        PresetRepository repository = PresetRepository.getInstance();
        OutputPreset sslPreset = repository.getPreset(basicPreset.protocol, basicPreset.address, basicPreset.port);
        if (sslPreset == null) {
            /* If there is no database entry corresponding to the above protocol/address/port combo,
             * we treat this as a default. So grab the values from default certs */
            List<OutputPreset> sslDefaults = repository.defaultsByProtocol(basicPreset.protocol);
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
}
