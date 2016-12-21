package com.alesharik.webserver.main;

import com.alesharik.webserver.api.KeySaver;
import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.api.sharedStorage.annotations.SharedValueSetter;
import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;
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
import lombok.SneakyThrows;
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
import java.util.Date;

import static com.alesharik.webserver.main.Main.USER_DIR;

/**
 * This class used fo run and shutdown server.In this class load configuration, initialize FileManager, OldPluginManager
 * and start server
 */
@Prefix("[ServerController]")
@UseSharedStorage("serverFolders")
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
            routerServer = new RouterServer(configuration.getInt("routerServer.port"), configuration.getString("routerServer.host"), configuration.getInt("routerServer.threadCount"));
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
            boolean logRequests = configuration.getBoolean("webServer.logRequests");
            File logFile = null;
            if(logRequests) {
                Date date = new Date();
                date.setTime(System.currentTimeMillis());
                logFile = new File(configuration.getString("webServer.logFile").replace("./", Main.USER_DIR.getPath() + "/").replace("{$time}", date.toString().replace(" ", "_")));
                if(!logFile.exists()) {
                    if(!logFile.createNewFile()) {
                        Logger.log("Can't create new request log file! Disabling request logging...");
                        logRequests = false;
                    }
                }
            }
            if(isEnabled(IS_CONTROL_SERVER_FLAG)) {
                server = new ControlServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, new AdminDataHolder(serverPassword), pluginDataHolder, logRequests, logFile);
            } else {
                server = new MainServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, this, pluginDataHolder, logRequests, logFile);

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
                KeySaver.saveKeyToFile(secretKey, keyFile);
            } catch (InvalidKeySpecException | InvalidKeyException e) {
                Logger.log(e);
            }
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
        fileBasedConfigurationBuilder.setAutoSave(false);
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

            checkProperty("main.isRepositorySnapshotEnabled", true, configuration);
            checkProperty("main.repositorySnapshotFile", "./repository.snapshot", configuration);
            checkProperty("main.repositorySnapshotDelay", 1000, configuration);

            checkProperty("webServer.enabled", true, configuration);
            checkProperty("webServer.port", 8080, configuration);
            checkProperty("webServer.host", "default", configuration);
            checkProperty("webServer.login", StringCipher.encrypt("admin", serverPassword), configuration);
            checkProperty("webServer.password", StringCipher.encrypt("admin", serverPassword), configuration);
            checkProperty("webServer.isControlServer", false, configuration);
            checkProperty("webServer.logRequests", true, configuration);
            checkProperty("webServer.logFile", "./logs/requestLog-{$time}", configuration); // {$time} replaced by current time TODO write custom formats


            checkProperty("microserviceClient.enabled", true, configuration);
            checkProperty("microserviceClient.port", 4000, configuration);
            checkProperty("microserviceClient.routerHost", "default", configuration);
            checkProperty("microserviceClient.routerPort", 4001, configuration);

            checkProperty("microserviceServer.enabled", true, configuration);
            checkProperty("microserviceServer.port", 4000, configuration);
            checkProperty("microserviceServer.host", "default", configuration);
            checkProperty("microserviceServer.routerHost", "default", configuration);
            checkProperty("microserviceServer.routerPort", 4001, configuration);
            checkProperty("microserviceServer.routerThreadCount", 5, configuration);

            checkProperty("routerServer.enabled", true, configuration);
            checkProperty("routerServer.host", "default", configuration);
            checkProperty("routerServer.port", 4001, configuration);
            checkProperty("routerServer.threadCount", 20, configuration);

            checkProperty("folders.logs", "./logs/", configuration);
            checkProperty("folders.plugins", "./plugins/", configuration);
            checkProperty("folders.www", "./www/", configuration);
            checkProperty("folders.dashboard", "./serverDashboard/", configuration);
            fileBasedConfigurationBuilder.save();

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

    @SneakyThrows
    private void loadConfigVars() {
        File logsFolder = new File(configuration.getString("folders.logs").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!logsFolder.exists()) {
            Logger.log("Can't find logging folder. Create new one...");
            if(!logsFolder.mkdir()) {
                throw new Error("Can't create new logging folder! Stopping...");
            }
        }
        setLogFolder(logsFolder);
        File pluginsFolder = new File(configuration.getString("folders.plugins").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!pluginsFolder.exists()) {
            Logger.log("Can't find plugins folder. Create new one...");
            if(!pluginsFolder.mkdir()) {
                throw new Error("Can't create new plugins folder! Stopping...");
            }
        }
        setPluginFolder(pluginsFolder);
        File wwwFolder = new File(configuration.getString("folders.www").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!wwwFolder.exists()) {
            Logger.log("Can't find www folder. Create new one...");
            if(!wwwFolder.mkdir()) {
                throw new Error("Can't create new www folder! Stopping...");
            }
        }
        setWwwFolder(wwwFolder);
        File dashboardFolder = new File(configuration.getString("folders.dashboard").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!dashboardFolder.exists()) {
            Logger.log("Can't find dashboard folder. Create new one...");
            if(!dashboardFolder.mkdir()) {
                throw new Error("Can't create new dashboard folder! Stopping...");
            }
        }
        setDashboardFolder(dashboardFolder);

        Logger.setupLogger(new File(logsFolder + generateLogName()));

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

        SerialRepository.snapsotEnabled(configuration.getBoolean("main.isRepositorySnapshotEnabled"));
        SerialRepository.setUpdateTime(configuration.getLong("main.repositorySnapshotDelay"));
        String snapshotFileString = configuration.getString("main.repositorySnapshotFile");
        if(snapshotFileString.startsWith("./")) {
            snapshotFileString = snapshotFileString.replace("./", Main.USER_DIR.getPath() + "/");
        }
        File snapshotFile = new File(snapshotFileString);
        if(snapshotFile.isDirectory() || !snapshotFile.canRead() || !snapshotFile.canWrite()) {
            Logger.log("Snapshot file " + snapshotFileString + " can't be used! Disabling snapshot...");
            SerialRepository.snapsotEnabled(false);
        }
        if(!snapshotFile.exists()) {
            if(!snapshotFile.createNewFile()) {
                Logger.log("Can't create " + snapshotFileString + "! Disabling snapshot...");
                SerialRepository.snapsotEnabled(false);
            }
        }
        SerialRepository.setSnapshotFile(snapshotFile);
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
            FileBasedBuilderParameters properties = new Parameters().fileBased();
            FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder
                    = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(properties
                            .setFileName("config.properties")
                            .setThrowExceptionOnMissing(true)
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(';')));
            fileBasedConfigurationBuilder.setAutoSave(true);
            try {
                Configuration config = fileBasedConfigurationBuilder.getConfiguration();
                config.setProperty("webServer.login", StringCipher.encrypt(newLogin, serverPassword));
                config.setProperty("webServer.password", StringCipher.encrypt(newPassword, serverPassword));
            } catch (ConfigurationException e) {
                Logger.log("Can't set new password and login!");
                Logger.log(e);
            }
        } else {
            throw new SecurityException();
        }
    }

    //TODO write this
    private void checkServerDashboard() {

    }

    @SharedValueSetter("log")
    private void setLogFolder(File folder) {
    }

    @SharedValueSetter("plugin")
    private void setPluginFolder(File folder) {
    }

    @SharedValueSetter("www")
    private void setWwwFolder(File folder) {
    }

    @SharedValueSetter("dashboard")
    private void setDashboardFolder(File folder) {
    }

    private static String generateLogName() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return "/Log-" + date.toString().replace(" ", "_");
    }
}
