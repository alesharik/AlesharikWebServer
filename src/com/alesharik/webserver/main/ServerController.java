package com.alesharik.webserver.main;

import com.alesharik.webserver.api.KeyHolder;
import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.control.dataHolding.AdminDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.main.server.ControlServer;
import com.alesharik.webserver.main.server.WebServer;
import com.alesharik.webserver.plugin.PluginManager;
import com.alesharik.webserver.plugin.PluginManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.BaseAccessManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManagerBuilder;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import one.nio.mem.OutOfMemoryException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

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
    private Configuration configuration;
    private String serverPassword;
    private Server server;
    private FileManager mainFileManager;
    private String host;
    private int port;
    private boolean isStarted = false;

    private final BaseAccessManagerBuilder baseAccessManagerBuilder = new BaseAccessManagerBuilder();
    private final ControlAccessManagerBuilder controlAccessManagerBuilder = new ControlAccessManagerBuilder();
    private final ServerAccessManagerBuilder serverAccessManagerBuilder = new ServerAccessManagerBuilder();

    private PluginManager pluginManager;
    /**
     * Init all needed systems
     */
    public ServerController() {
        try {
            loadServerPassword();
            loadConfig();
            host = Main.HOST;
            port = configuration.getInt("port");

            if(configuration.getBoolean("isControlServer")) {
                initControlServer();
            } else {
                initMainServer();
            }
            server.setupServerAccessManagerBuilder(serverAccessManagerBuilder);
            baseAccessManagerBuilder.setFileManager(mainFileManager);
            Logger.log("Server successfully initialized");

            pluginManager = new PluginManagerBuilder()
                    .setBaseAccessManager(baseAccessManagerBuilder.build())
                    .setControlAccessManager(controlAccessManagerBuilder.build())
                    .setServerAccessManager(serverAccessManagerBuilder.build())
                    .build();
            pluginManager.addPlugin(new File(Main.USER_DIR + "/plugins/test"));
            pluginManager.loadPlugins();
            pluginManager.start();
        } catch (IOException | ConfigurationException e) {
            Logger.log(e);
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
            password = StringCipher.decrypt(new String(Files.readAllBytes(passwordFile.toPath())), null, secretKey);
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
            Logger.log("Can't decode password!Creating new one...");
        }
        try {
            if(password.isEmpty()) {
                password = Helpers.getRandomString(24);
                Files.write(passwordFile.toPath(), StringCipher.encrypt(password, null, secretKey).getBytes());
            }
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidKeyException e) {
            Logger.log(e);
        }
        return password;
    }

    private SecretKey loadSecretKey(File keyFile) throws IOException {
        SecretKey secretKey = null;
        try {
            secretKey = KeyHolder.loadKeyFromFile(keyFile, StringCipher.DESEDE_ENCRYPTION_SCHEME);
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
            KeyHolder.saveKeyToFile(secretKey, keyFile);
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
        tryLoadConfig(fileBasedConfigurationBuilder);
    }

    /**
     * Try to load config(load config and set all empty fields to its default values)
     *
     * @param fileBasedConfigurationBuilder builder with config
     */
    private void tryLoadConfig(FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder) {
        try {
            this.configuration = fileBasedConfigurationBuilder.getConfiguration();

            if(!configuration.containsKey("port")) {
                configuration.addProperty("port", 7000);
            }
            if(!configuration.containsKey("isControlServer")) {
                configuration.addProperty("isControlServer", false);
            }
            if(!configuration.containsKey("login")) {
                configuration.addProperty("login", StringCipher.encrypt("admin", serverPassword));
            }
            if(!configuration.containsKey("password")) {
                configuration.addProperty("password", StringCipher.encrypt("admin", serverPassword));
            }

            Logger.log("Config loaded");
        } catch (ConfigurationException | InvalidKeyException | IllegalBlockSizeException
                | UnsupportedEncodingException | BadPaddingException | InvalidKeySpecException e) {
            Logger.log("Can't initialize config!");
            Logger.log(e);
            Main.shutdown();
        }
    }

    //TODO Set to hold and check
    private void initMainServer() {
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

        server = new WebServer(host, port, mainFileManager, this);
        Logger.log("Main server initialized");
    }

    //TODO Set to hold and check
    private void initControlServer() throws ConfigurationException, IOException {
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

        server = new ControlServer(host, port, mainFileManager, new AdminDataHolder(serverPassword));
        ((ControlServer) server).setupControlAccessManagerBuilder(controlAccessManagerBuilder);
        Logger.log("Control server initialized");
    }

    /**
     * Shutdown server
     */
    public void shutdown() {
        if(isStarted) {
            server.shutdown();
            Logger.log("Server stopped");
        }
    }

    /**
     * Start server
     */
    public void start() throws IOException {
        if(!isStarted) {
            server.start();
            Logger.log("Server started at " + host + ":" + port);
            isStarted = true;
        }
    }

    public boolean isLogPassValid(String logPass) throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
        return LoginPasswordCoder.isEquals(StringCipher.decrypt(configuration.getString("login"), serverPassword),
                StringCipher.decrypt(configuration.getString("password"), serverPassword), logPass);
    }

    //TODO write this
    private void checkServerDashboard() {

    }
}
