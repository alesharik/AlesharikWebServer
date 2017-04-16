package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.control.ControlSocketServerModule;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Prefixes;
import one.nio.lock.RWLock;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Prefixes({"[ControlSocket]", "[ControlSocketServer]"})
@ThreadSafe
public class ControlSocketServerModuleImpl implements ControlSocketServerModule {
    private final RWLock lock = new RWLock();

    private String login;
    private String password;
    private Set<String> hosts;
    private int port;
    private File keystoreFile;
    private String keystorePassword;
    private Map<String, ControlSocketServerConnectionManager> connectionManagers = new ConcurrentHashMap<>();

    @Override
    public int connectionCount() {
        return connectionManagers.values().stream()
                .map(ControlSocketServerConnectionManager::getConnectionCount)
                .reduce(Integer::sum)
                .orElse(-1);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Set<String> getListenAddresses() {
        Set<String> strings;
        try {
            lock.lockRead();
            strings = Collections.unmodifiableSet(hosts);
        } finally {
            lock.unlockRead();
        }
        return strings;
    }

    @Override
    public void parse(@Nullable Element configNode) {
        if(configNode == null) {
            throw new ConfigurationParseError("ControlSocketClient must have configuration!");
        } else {
            Element keystoreElem = XmlHelper.getXmlElement("keystore", configNode, true);

            File keystoreFile = XmlHelper.getFile("file", keystoreElem, true);
            String keystorePassword = XmlHelper.getString("password", keystoreElem, true);

            int port = Integer.parseInt(XmlHelper.getString("port", configNode, true));
            Set<String> hosts = new HashSet<>(XmlHelper.getList("hosts", "host", configNode, true));

            Element auth = XmlHelper.getXmlElement("authentication", configNode, true);
            String login = XmlHelper.getString("login", auth, true);
            String password = XmlHelper.getString("password", auth, true);
            try {
                lock.lockWrite();
                this.keystoreFile = keystoreFile;
                this.keystorePassword = keystorePassword;
                this.port = port;
                this.hosts = hosts;
                this.login = login;
                this.password = password;
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
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream stream = new FileInputStream(keystoreFile);) {
                keyStore.load(stream, keystorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

            SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();

            for(String host : hosts) {
                SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port, 0, Inet4Address.getByName(host));
                ControlSocketServerConnectionManager connectionManager = new ControlSocketServerConnectionManager(serverSocket, login, password);
                connectionManager.start();
                connectionManagers.put(host, connectionManager);
            }
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {
        connectionManagers.values().forEach(ControlSocketServerConnectionManager::shutdown);
    }

    @Override
    public void shutdownNow() {
        connectionManagers.values().forEach(ControlSocketServerConnectionManager::shutdownNow);
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
