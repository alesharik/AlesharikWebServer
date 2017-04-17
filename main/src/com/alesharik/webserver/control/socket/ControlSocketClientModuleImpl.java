package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.control.ControlSocketClientModule;
import com.alesharik.webserver.api.control.ControlSocketClientModuleMXBean;
import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Prefixes;
import one.nio.lock.RWLock;
import one.nio.mgt.Management;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicInteger;

@Prefixes({"[ControlSocket]", "[ControlSocketClient]"})
public class ControlSocketClientModuleImpl implements ControlSocketClientModule {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final RWLock lock = new RWLock();
    private final int id = COUNTER.getAndIncrement();
    private final ThreadGroup workerThreadGroup = new ThreadGroup("ControlSocketClientWorkers-" + id);

    private File keystoreFile;
    private String keystorePassword;

    private ControlSocketClientConnectionPool connectionPool;

    @Override
    public int getConnectionCount() {
        return 0;
    }

    @Override
    public void parse(@Nullable Element configNode) {
        if(configNode == null) {
            throw new ConfigurationParseError("ControlSocketClient must have configuration!");
        } else {
            Element keystoreElem = XmlHelper.getXmlElement("keystore", configNode, true);

            File keystoreFile = XmlHelper.getFile("file", keystoreElem, true);
            String keystorePassword = XmlHelper.getString("password", keystoreElem, true);
            try {
                lock.lockWrite();
                this.keystoreFile = keystoreFile;
                this.keystorePassword = keystorePassword;
            } finally {
                lock.unlockWrite();
            }
        }
    }

    @Override
    public void reload(@Nullable Element configNode) {
        shutdown();
        parse(configNode);
        start();
    }

    @Override
    public void start() {
        Management.registerMXBean(this, ControlSocketClientModuleMXBean.class, "ControlSocketClient-" + id);
        File keystoreFile;
        String keystorePassword;

        try {
            lock.lockRead();
            keystoreFile = this.keystoreFile;
            keystorePassword = this.keystorePassword;
        } finally {
            lock.unlockRead();
        }

        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream stream = new FileInputStream(keystoreFile)) {
                keyStore.load(stream, keystorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

            connectionPool = new ControlSocketClientConnectionPool(workerThreadGroup, sslContext.getSocketFactory());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        connectionPool.shutdown();
        Management.unregisterMXBean("ControlSocketClient-" + id);
    }

    @Override
    public void shutdownNow() {
        connectionPool.shutdown();
        Management.unregisterMXBean("ControlSocketClient-" + id);
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }

    @Override
    public ControlSocketClientConnection newConnection(String host, int port, ControlSocketClientConnection.Authenticator authenticator) throws IOException {
        return connectionPool.newConnection(host, port, authenticator);
    }
}
