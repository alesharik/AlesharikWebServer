/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.main;

import com.alesharik.webserver.api.KeySaver;
import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.api.sharedStorage.annotations.SharedValueSetter;
import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.main.server.ControlServer;
import com.alesharik.webserver.main.server.MainServer;
import com.alesharik.webserver.microservices.client.MicroserviceClient;
import com.alesharik.webserver.microservices.server.MicroserviceServer;
import com.alesharik.webserver.router.RouterServer;
import lombok.SneakyThrows;
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
 * and setup server
 */
@Deprecated
@Prefixes("[ServerController]")
public final class ServerController {
    private final ConfigValues configValues = new ConfigValues();

    private Configuration configuration;
    private String serverPassword;

    private FileManager mainFileManager;
    private Server server;
    private int config = 0;
    private DashboardDataHolder dashboardDataHolder = new DashboardDataHolder();
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
//            SharedStorageManager.addAccessFilter("config", (clazz, type, fieldName) -> {
//                switch (type) {
//                    case SET:
//                        return ConfigValues.class.equals(clazz);
//                    case SET_EXTERNAL:
//                    case ADD_FILTER:
//                    case CLEAR:
//                        return false;
//                    case GET:
//                    case GET_EXTERNAL:
//                        return true;
//                    default:
//                        return false;
//                }
//            });

            loadServerPassword();
            loadConfig();
            Logger.log("Config loaded!");
            initFileManager();
            Logger.log("FileManager created!");

            initWebServer();
            initMicroserviceClient();
            initMicroserviceServer();
            initRouterServer();

            Logger.log("Server successfully initialized");
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
                server = new ControlServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, new AdminDataStorageImpl(), dashboardDataHolder, logRequests, logFile);
            } else {
                server = new MainServer(configuration.getString("webServer.host"), configuration.getInt("webServer.port"), mainFileManager, this, dashboardDataHolder, logRequests, logFile);

            }
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
                password = Utils.getRandomString(24);
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
                String randomString = Utils.getRandomString(24);
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
        configValues.setLogFolder(logsFolder);
        File pluginsFolder = new File(configuration.getString("folders.plugins").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!pluginsFolder.exists()) {
            Logger.log("Can't find plugins folder. Create new one...");
            if(!pluginsFolder.mkdir()) {
                throw new Error("Can't create new plugins folder! Stopping...");
            }
        }
        configValues.setPluginFolder(pluginsFolder);
        File wwwFolder = new File(configuration.getString("folders.www").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!wwwFolder.exists()) {
            Logger.log("Can't find www folder. Create new one...");
            if(!wwwFolder.mkdir()) {
                throw new Error("Can't create new www folder! Stopping...");
            }
        }
        configValues.setWwwFolder(wwwFolder);
        File dashboardFolder = new File(configuration.getString("folders.dashboard").replace("./", Main.USER_DIR.getPath() + "/"));
        if(!dashboardFolder.exists()) {
            Logger.log("Can't find dashboard folder. Create new one...");
            if(!dashboardFolder.mkdir()) {
                throw new Error("Can't create new dashboard folder! Stopping...");
            }
        }
        configValues.setDashboardFolder(dashboardFolder);

        setupSharedStorage(configuration);

        Logger.setupLogger(new File(logsFolder + generateLogName()), 200); //TODO remove

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

        SerialRepository.snapshotEnabled(configuration.getBoolean("main.isRepositorySnapshotEnabled"));
        SerialRepository.setUpdateTime(configuration.getLong("main.repositorySnapshotDelay"));
        String snapshotFileString = configuration.getString("main.repositorySnapshotFile");
        if(snapshotFileString.startsWith("./")) {
            snapshotFileString = snapshotFileString.replace("./", Main.USER_DIR.getPath() + "/");
        }
        File snapshotFile = new File(snapshotFileString);
        configValues.setRepositorySnapshotFile(snapshotFile);
        if(snapshotFile.isDirectory() || !snapshotFile.canRead() || !snapshotFile.canWrite()) {
            Logger.log("Snapshot file " + snapshotFileString + " can't be used! Disabling snapshot...");
            SerialRepository.snapshotEnabled(false);
        }
        if(!snapshotFile.exists()) {
            if(!snapshotFile.createNewFile()) {
                Logger.log("Can't create " + snapshotFileString + "! Disabling snapshot...");
                SerialRepository.snapshotEnabled(false);
            }
        }
        SerialRepository.setSnapshotFile(snapshotFile);
    }

    private void setupSharedStorage(Configuration configuration) {
        configValues.setRepositorySnapshotEnabled(configuration.getBoolean("main.isRepositorySnapshotEnabled"));
        configValues.setRepositorySnapshotDelay(configuration.getLong("main.repositorySnapshotDelay"));

        configValues.setWebServerEnabled(configuration.getBoolean("webServer.enabled"));
        configValues.setWebServerPort(configuration.getInt("webServer.port"));
        configValues.setWebServerHost(configuration.getString("webServer.host"));
        configValues.setWebServerControlServer(configuration.getBoolean("webServer.isControlServer"));
        configValues.setWebServerLogRequests(configuration.getBoolean("webServer.logRequests"));

        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        File logFile = new File(configuration.getString("webServer.logFile").replace("./", Main.USER_DIR.getPath() + "/").replace("{$time}", date.toString().replace(" ", "_")));
        configValues.setWebServerLogFile(logFile);

        configValues.setMicroserviceClientEnabled(configuration.getBoolean("microserviceClient.enabled"));
        configValues.setMicroserviceClientPort(configuration.getInt("microserviceClient.port"));
        configValues.setMicroserviceClientRouterHost(configuration.getString("microserviceClient.routerHost"));
        configValues.setMicroserviceClientRouterPort(configuration.getInt("microserviceClient.routerPort"));

        configValues.setMicroserviceServerEnabled(configuration.getBoolean("microserviceServer.enabled"));
        configValues.setMicroserviceServerPort(configuration.getInt("microserviceServer.port"));
        configValues.setMicroserviceServerHost(configuration.getString("microserviceServer.host"));
        configValues.setMicroserviceServerRouterHost(configuration.getString("microserviceServer.routerHost"));
        configValues.setMicroserviceServerRouterPort(configuration.getInt("microserviceServer.routerPort"));

        configValues.setRouterServerEnabled(configuration.getBoolean("routerServer.enabled"));
        configValues.setRouterServerHost(configuration.getString("routerServer.host"));
        configValues.setRouterServerPort(configuration.getInt("routerServer.port"));
        configValues.setRouterServerThreadCount(configuration.getInt("routerServer.threadCount"));
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

    private static String generateLogName() {
        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        return "/Log-" + date.toString().replace(" ", "_");
    }

    @UseSharedStorage("config")
    private static final class ConfigValues {
        //====================Main====================\\

        @SharedValueSetter("main.isRepositorySnapshotEnabled")
        public void setRepositorySnapshotEnabled(boolean value) {
        }

        @SharedValueSetter("main.repositorySnapshotFile")
        public void setRepositorySnapshotFile(File file) {
        }

        @SharedValueSetter("main.repositorySnapshotDelay")
        public void setRepositorySnapshotDelay(long delay) {
        }

        //====================Web server====================\\

        @SharedValueSetter("webServer.enabled")
        public void setWebServerEnabled(boolean value) {
        }

        @SharedValueSetter("webServer.port")
        public void setWebServerPort(int port) {
        }

        @SharedValueSetter("webServer.host")
        public void setWebServerHost(String host) {
        }

        @SharedValueSetter("webServer.isControlServer")
        public void setWebServerControlServer(boolean value) {
        }

        @SharedValueSetter("webServer.logRequests")
        public void setWebServerLogRequests(boolean value) {
        }

        @SharedValueSetter("webServer.logFile")
        public void setWebServerLogFile(File file) {
        }

        //====================Microservice client====================\\

        @SharedValueSetter("microserviceClient.enabled")
        public void setMicroserviceClientEnabled(boolean value) {
        }

        @SharedValueSetter("microserviceClient.port")
        public void setMicroserviceClientPort(int port) {
        }

        @SharedValueSetter("microserviceClient.routerHost")
        public void setMicroserviceClientRouterHost(String value) {
        }

        @SharedValueSetter("microserviceClient.routerPort")
        public void setMicroserviceClientRouterPort(int port) {
        }

        //====================Microservice server====================\\

        @SharedValueSetter("microserviceServer.enabled")
        public void setMicroserviceServerEnabled(boolean value) {
        }

        @SharedValueSetter("microserviceServer.port")
        public void setMicroserviceServerPort(int port) {
        }

        @SharedValueSetter("microserviceServer.host")
        public void setMicroserviceServerHost(String value) {
        }

        @SharedValueSetter("microserviceServer.routerHost")
        public void setMicroserviceServerRouterHost(String value) {
        }

        @SharedValueSetter("microserviceServer.routerPort")
        public void setMicroserviceServerRouterPort(int port) {
        }

        //====================Router server====================\\

        @SharedValueSetter("routerServer.enabled")
        public void setRouterServerEnabled(boolean value) {
        }

        @SharedValueSetter("routerServer.host")
        public void setRouterServerHost(String host) {
        }

        @SharedValueSetter("routerServer.port")
        public void setRouterServerPort(int port) {
        }

        @SharedValueSetter("routerServer.threadCount")
        public void setRouterServerThreadCount(int value) {
        }

        //====================Folders====================\\

        @SharedValueSetter("folder.logs")
        public void setLogFolder(File folder) {
        }

        @SharedValueSetter("folders.plugin")
        public void setPluginFolder(File folder) {
        }

        @SharedValueSetter("folders.www")
        public void setWwwFolder(File folder) {
        }

        @SharedValueSetter("folders.dashboard")
        public void setDashboardFolder(File folder) {
        }
    }
}
