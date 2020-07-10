package com.jon.cot.common.service;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.jon.cot.common.cot.CursorOnTarget;
import com.jon.cot.common.presets.OutputPreset;
import com.jon.cot.common.presets.PresetRepository;
import com.jon.cot.common.utils.Constants;
import com.jon.cot.common.utils.Key;
import com.jon.cot.common.utils.PrefUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import timber.log.Timber;

public class SslCotThread extends TcpCotThread {
    private SSLSocket socket;
    private OutputStream outputStream;

    SslCotThread(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    void shutdown() {
        super.shutdown();
        closeFromMainThread(outputStream);
        closeFromMainThread(socket);
        outputStream = null;
        socket = null;
    }

    @Override
    protected void sendToDestination(CursorOnTarget cot) throws IOException {
        try {
            outputStream.write(cot.toBytes(dataFormat));
            Timber.i("Sent cot: %s", cot.callsign);
        } catch (NullPointerException | SocketException e) {
            /* Thrown when the thread is cancelled from another thread and we try to access the sockets */
            shutdown();
        }
    }

    @Override
    protected void openSocket() throws Exception {
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
        socket = (SSLSocket) sslContext.getSocketFactory().createSocket(destIp, destPort);
        socket.setSoTimeout(Constants.TCP_SOCKET_TIMEOUT_MILLISECONDS);
        outputStream = socket.getOutputStream();
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

    /* This workaround is only necessary because I was getting NetworkOnMainThreadExceptions when
     * closing the SSLSocket via shutdown() from the UI thread. This wasn't an issue with TCP/UDP
     * so this is just a patch to deal with it for now. */
    private void closeFromMainThread(Closeable closeable) {
        try {
            new CloseSocketTask().execute(closeable).get();
        } catch (ExecutionException | InterruptedException e) {
            /* Ignore */
        }
    }

    private static class CloseSocketTask extends AsyncTask<Closeable, Void, Void> {
        protected Void doInBackground(Closeable... closeables) {
            for (Closeable closeable : closeables) {
                try { if (closeable != null) closeable.close(); }
                catch (IOException e) { Timber.e(e); }
            }
            return null;
        }
    }
}
