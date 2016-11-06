package com.alesharik.webserver.main;

import com.alesharik.webserver.api.KeySaver;
import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.control.dashboard.PluginDataHolder;
import com.alesharik.webserver.control.dataHolding.AdminDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.main.server.ControlServer;
import com.alesharik.webserver.main.server.MainServer;
import com.alesharik.webserver.microservices.client.MicroserviceClient;
import com.alesharik.webserver.microservices.server.MicroserviceServer;
import com.alesharik.webserver.plugin.PluginManager;
import com.alesharik.webserver.plugin.PluginManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.BaseAccessManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import com.alesharik.webserver.router.RouterServer;
import one.nio.mem.OutOfMemoryException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.glassfish.grizzly.utils.Charsets;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import static com.alesharik.webserver.main.Main.USER_DIR;

/**
 * This class used fo run and shutdown server.In this class load configuration, initialize FileManager, OldPluginManager
 * and start server
 */
@Prefix("[ServerController]")
public final class ServerController {

    private final BaseAccessManagerBuilder baseAccessManagerBuilder = new BaseAccessManagerBuilder();
    private final ControlAccessManagerBuilder controlAccessManagerBuilder = new ControlAccessManagerBuilder();
    private final ServerAccessManagerBuilder serverAccessManagerBuilder = new ServerAccessManagerBuilder();

    private MicroserviceAccessManagerBuilder microserviceAccessManagerBuilder;

    private PluginManager pluginManager;

    private Configuration configuration;
    private String serverPassword;

    private FileManager mainFileManager;
    private Server server;
    private int config = 0;
    private PluginDataHolder pluginDataHolder = new PluginDataHolder();
    private boolean isStarted = false;

    private static final int IS_CONTROL_SERVER_FLAG = 1;
    private static final int MICROSERVICE_CLIENT = 2;
    private static final int MICROSERVICE_SERVER = 4;
    private static final int ROUTER_SERVER = 8;
    private static final int WEB_SERVER = 16;

    private MicroserviceClient microserviceClient = null;
    private MicroserviceServer microserviceServer = null;
    private RouterServer routerServer = null;

    /**
     * Init all needed systems
     */
    public ServerController() {
        try {
            loadServerPassword();
            loadConfig();
            Logger.log("Config loaded!");
            initFileManager();
            Logger.log("FileManager created!");

            initWebServer();
            initMicroserviceClient();
            initMicroserviceServer();
            initRouterServer();
            baseAccessManagerBuilder.setFileManager(mainFileManager);
            baseAccessManagerBuilder.setPluginDataHolder(pluginDataHolder);

            Logger.log("Server successfully initialized");

            pluginManager = new PluginManagerBuilder()
                    .setBaseAccessManager(baseAccessManagerBuilder.build())
                    .setControlAccessManager(controlAccessManagerBuilder.build())
                    .setServerAccessManager(serverAccessManagerBuilder.build())
//                    .isMicroserviceServer(false)
//                    .isRouterServer(configuration.getBoolean("isRouterServer"))
                    .build();
            pluginManager.addPlugin(new File(Main.USER_DIR + "/plugins/test"));
            pluginManager.loadPlugins();
            pluginManager.start();
        } catch (IOException | ConfigurationException e) {
            Logger.log(e);
        }
    }

    private void initRouterServer() {
        if(isEnabled(ROUTER_SERVER)) {
            routerServer = new RouterServer(configuration.getInt("routerServer.port"), configuration.getString("routerServer.host"), RouterServer.WorkingMode.ADVANCED);
            Logger.log("Router server loaded!");
        }
    }

    private void initMicroserviceServer() {
        if(isEnabled(MICROSERVICE_SERVER)) {
            microserviceServer = new MicroserviceServer(configuration.getString("microserviceServer.host"), configuration.getInt("microserviceServer.port"), MicroserviceServer.WorkingMode.ADVANCED, configuration.getString("microserviceServer.routerHost"), configuration.getInt("microserviceServer.routerPort"));
//            microserviceAccessManagerBuilder.setServer(microserviceServer);
            Logger.log("Microservice server loaded!");
        }
    }

    private void initMicroserviceClient() {
        if(isEnabled(MICROSERVICE_CLIENT)) {
            microserviceClient = new MicroserviceClient(MicroserviceClient.WorkingMode.ADVANCED, configuration.getString("microserviceClient.routerHost"), configuration.getInt("microserviceClient.routerPort"));
//            microserviceAccessManagerBuilder.setClient(microserviceClient);
            Logger.log("Microservice client loaded!");
        }
    }

    private void initFileManager() {
        if(isEnabled(IS_CONTROL_SERVER_FLAG)) {
            checkServerDashboard();
            try {
                mainFileManager = new FileManager(Main.SERVER_DASHBOARD, FileManager.FileHoldingMode.HOLD_AND_CHECK,
                        FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES,
                        FileManager.FileHoldingParams.DISABLE_IGNORE_LOGS_FOLDER);
            } catch (OutOfMemoryException e) {
                Logger.log("Can't initialize file manager with holding! Cause: " + e.getLocalizedMessage());
                mainFileManager = new FileManager(Main.SERVER_DASHBOARD, FileManager.FileHoldingMode.NO_HOLD,
                        FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES,
                        FileManager.FileHoldingParams.DISABLE_IGNORE_LOGS_FOLDER);
            }
        } else {
            try {
                mainFileManager = new FileManager(Main.WWW, FileManager.FileHoldingMode.HOLD_AND_CHECK,
                        FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES,
                        FileManager.FileHoldingParams.DISABLE_IGNORE_LOGS_FOLDER);
            } catch (OutOfMemoryException e) {
                Logger.log("Can't initialize file manager with holding! Cause: " + e.getLocalizedMessage());
                mainFileManager = new FileManager(Main.WWW, FileManager.FileHoldingMode.NO_HOLD,
                        FileManager.FileHoldingParams.IGNORE_HIDDEN_FILES,
                        FileManager.FileHoldingParams.DISABLE_IGNORE_LOGS_FOLDER);
            }
        }
    }

    private void initWebServer() throws IOException, ConfigurationException {
        if(isEnabled(WEB_SERVER)) {
            if(isEnabled(IS_CONTROL_SERVER_FLAG)) {
                server = new ControlServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, new AdminDataHolder(serverPassword), pluginDataHolder);
            } else {
                server = new MainServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, this, pluginDataHolder);

            }
            ((WebServer) server).setupServerAccessManagerBuilder(serverAccessManagerBuilder);
            Logger.log("WebServer loaded!");
        }
    }



    private void loadServerPassword() throws IOException {
        File keyFile = new File(USER_DIR + "/serverPassword.key");
        File passwordFile = new File(USER_DIR + "/serverPassword");
        if(!keyFile.exists()) {
            if(!keyFile.createNewFile()) {
                Logger.log("Can't create file " + keyFile);
            }
        }
        if(!passwordFile.exists()) {
            if(!passwordFile.createNewFile()) {
                Logger.log("Can't create file " + passwordFile);
            }
        }

        SecretKey secretKey = loadSecretKey(keyFile);
        this.serverPassword = loadPassword(passwordFile, secretKey);
    }

    private String loadPassword(File passwordFile, SecretKey secretKey) throws IOException {
        String password = "";
        try {
            password = StringCipher.decrypt(new String(Files.readAllBytes(passwordFile.toPath()), Charsets.UTF8_CHARSET), null, secretKey);
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
            Logger.log("Can't decode password! Creating new one...");
        }
        try {
            if(password.isEmpty()) {
                password = Helpers.getRandomString(24);
                Files.write(passwordFile.toPath(), StringCipher.encrypt(password, null, secretKey).getBytes(Charsets.UTF8_CHARSET));
            }
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidKeyException e) {
            Logger.log(e);
        }
        return password;
    }

    private SecretKey loadSecretKey(File keyFile) throws IOException {
        SecretKey secretKey = null;
        try {
            secretKey = KeySaver.loadKeyFromFile(keyFile, StringCipher.DESEDE_ENCRYPTION_SCHEME);
        } catch (IOException | IllegalArgumentException e) {
            Logger.log("Can't load key from file " + keyFile + "! Creating new one...");
        }
        if(secretKey == null) {
            try {
                String randomString = Helpers.getRandomString(24);
                secretKey = StringCipher.generateKey(randomString);
            } catch (InvalidKeySpecException | InvalidKeyException e) {
                Logger.log(e);
            }
            KeySaver.saveKeyToFile(secretKey, keyFile);
        }
        return secretKey;
    }


    /**
     * Load config
     *
     * @throws IOException if anything happens
     */
    private void loadConfig() throws IOException {
        File configFile = new File(Main.USER_DIR + "/config.properties");
        if(!configFile.exists()) {
            if(!configFile.createNewFile()) {
                Logger.log("Can't create config file " + configFile + "!");
            }
        }

        FileBasedBuilderParameters properties = new Parameters().fileBased();
        FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(properties
                        .setFileName("config.properties")
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';')));
        fileBasedConfigurationBuilder.setAutoSave(true);
        setupConfig(fileBasedConfigurationBuilder);
        loadConfigVars();
    }

    /**
     * Try to load config(load config and set all empty fields to its default values)
     *
     * @param fileBasedConfigurationBuilder builder with config
     */
    private void setupConfig(FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder) {
        try {
            this.configuration = fileBasedConfigurationBuilder.getConfiguration();
            checkProperty("webServer.enabled", true, configuration);
            checkProperty("webServer.port", 8080, configuration);
            checkProperty("webServer.host", "default", configuration);
            checkProperty("webServer.login", StringCipher.encrypt("admin", serverPassword), configuration);
            checkProperty("webServer.password", StringCipher.encrypt("admin", serverPassword), configuration);
            checkProperty("webServer.isControlServer", false, configuration);

            checkProperty("microserviceClient.enabled", true, configuration);
            checkProperty("microserviceClient.port", 4000, configuration);
            checkProperty("microserviceClient.routerHost", "default", configuration);
            checkProperty("microserviceClient.routerPort", 4001, configuration);

            checkProperty("microserviceServer.enabled", true, configuration);
            checkProperty("microserviceServer.port", 4000, configuration);
            checkProperty("microserviceServer.host", "default", configuration);
            checkProperty("microserviceServer.routerHost", "default", configuration);
            checkProperty("microserviceServer.routerPort", 4001, configuration);

            checkProperty("routerServer.enabled", true, configuration);
            checkProperty("routerServer.host", "default", configuration);
            checkProperty("routerServer.port", 4001, configuration);

            String externalIp = Utils.getExternalIp();
            loadDefault("webServer.host", externalIp, configuration);
            loadDefault("microserviceClient.routerHost", externalIp, configuration);
            loadDefault("microserviceServer.host", externalIp, configuration);
            loadDefault("microserviceServer.routerHost", externalIp, configuration);
            loadDefault("routerServer.host", externalIp, configuration);
        } catch (ConfigurationException | InvalidKeyException | IllegalBlockSizeException | UnsupportedEncodingException | BadPaddingException | InvalidKeySpecException e) {
            Logger.log("Can't initialize config!");
            Logger.log(e);
        }
    }

    private void checkProperty(String name, Object defaultValue, Configuration configuration) {
        if(!configuration.containsKey(name)) {
            configuration.addProperty(name, defaultValue);
        }
    }

    private void loadDefault(String name, Object defaultValue, Configuration configuration) {
        if(configuration.getProperty(name).equals("default")) {
            configuration.setProperty(name, defaultValue);
        }
    }

    private void loadConfigVars() {
        if(configuration.getBoolean("webServer.isControlServer")) {
            config |= IS_CONTROL_SERVER_FLAG;
        }
        if(configuration.getBoolean("microserviceClient.enabled")) {
            config |= MICROSERVICE_CLIENT;
        }
        if(configuration.getBoolean("microserviceServer.enabled")) {
            config |= MICROSERVICE_SERVER;
        }
        if(configuration.getBoolean("routerServer.enabled")) {
            config |= ROUTER_SERVER;
        }
        if(configuration.getBoolean("webServer.enabled")) {
            config |= WEB_SERVER;
        }
    }

    private boolean isEnabled(int flag) {
        return (config & flag) == flag;
    }

    /**
     * Shutdown server
     */
    public void shutdown() throws IOException {
        if(isStarted) {
            if(isEnabled(WEB_SERVER)) {
                server.shutdown();
                Logger.log("Web server stopped");
            }
            if(isEnabled(MICROSERVICE_SERVER)) {
                microserviceServer.shutdown();
                Logger.log("Microservice server stopped");
            }
            if(isEnabled(MICROSERVICE_CLIENT)) {
                microserviceClient.shutdown();
                Logger.log("Microservice client stopped");
            }
            if(isEnabled(ROUTER_SERVER)) {
                routerServer.shutdown();
                Logger.log("Router server stopped");
            }
            isStarted = false;
        }
    }

    /**
     * Start server
     */
    public void start() throws IOException {
        if(!isStarted) {
            if(isEnabled(WEB_SERVER)) {
                server.start();
                Logger.log("Web server started at " + server.getHost() + ":" + server.getPort());
            }
            if(isEnabled(MICROSERVICE_SERVER)) {
                microserviceServer.start();
                Logger.log("Microservice server started at " + microserviceServer.getHost() + ":" + microserviceServer.getPort());
            }
            if(isEnabled(MICROSERVICE_CLIENT)) {
                microserviceClient.start();
                Logger.log("Microservice client started");
            }
            if(isEnabled(ROUTER_SERVER)) {
                routerServer.start();
                Logger.log("Router server started at " + routerServer.getHost() + ":" + routerServer.getPort());
            }
            isStarted = true;
        }
    }

    public boolean isLogPassValid(String logPass) throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
        return LoginPasswordCoder.isEquals(StringCipher.decrypt(configuration.getString("webServer.login"), serverPassword),
                StringCipher.decrypt(configuration.getString("webServer.password"), serverPassword), logPass);
    }

    public void setLoginPassword(String old, String newLogin, String newPassword) throws InvalidKeyException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException, UnsupportedEncodingException {
        if(isLogPassValid(old)) {
            configuration.setProperty("webServer.login", StringCipher.encrypt(newLogin, serverPassword));
            configuration.setProperty("webServer.password", StringCipher.encrypt(newPassword, serverPassword));
        } else {
            throw new SecurityException();
        }
    }

    //TODO write this
    private void checkServerDashboard() {

    }
}
