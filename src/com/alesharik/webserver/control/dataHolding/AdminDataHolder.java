package com.alesharik.webserver.control.dataHolding;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.main.Main;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.glassfish.grizzly.http.util.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class used for hold data admin login, password and data in encrypted state
 */
//TODO fix update password with test/test cannot decrypt file!
@Prefixes(value = {"[ServerControl]", "[AdminDataHolder]"})
public final class AdminDataHolder {
    private static final String ADMIN_DATA_FILE = "adminData.dat";
    private static final byte[] SALT = Base64Utils.decodeFast("D0FT7kTsc858gx595E04m1fB6ByyGCSSFpBv01wicz8=");
    private Configuration configuration;

    /**
     * Initialize new {@link AdminDataHolder}
     *
     * @param key encryption key. Length of key must be equals 24!
     */
    public AdminDataHolder(String key) throws ConfigurationException, IOException {
        File adminDataFile = new File(Main.USER_DIR + "/" + ADMIN_DATA_FILE);
        if(!adminDataFile.exists()) {
            if(!adminDataFile.createNewFile()) {
                Logger.log("Oops! Problem with creating holder file: " + adminDataFile);
                throw new IOException("Can't create file: " + adminDataFile);
            }
        }

        PropertiesBuilderParameters properties = new Parameters().properties();
        FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(properties
                        .setFileName(ADMIN_DATA_FILE)
                        .setIOFactory(new EncryptedIOFactory(key))
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';')));
        fileBasedConfigurationBuilder.setAutoSave(true);
        configuration = fileBasedConfigurationBuilder.getConfiguration();

        initBaseConfiguration();
        Logger.log("AdminDataHolder successfully initialized!");
    }

    private void initBaseConfiguration() {
        if(!configuration.containsKey("hashedPass")) {
            setPassword(LoginPasswordCoder.encode("admin", "admin"));
        }
    }

    /**
     * Check is login and password are correct
     */
    public boolean check(String login, String password) {
        return check(LoginPasswordCoder.encode(login, password));
    }

    /**
     * Check is login and password are correct
     */
    public boolean check(String logpass) {
        try {
            byte[] base64HashedLogPass = StringCipher.hashString(logpass, SALT, 50, 256);
            return configuration.getArray(Byte.class, "hashedPass").equals(base64HashedLogPass);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Logger.log(e);
        }
        return false;
    }

    /**
     * Update login and password
     */
    public void updateLoginPassword(String oldLogin, String oldPassword, String newLogin, String newPassword) {
        if(check(oldLogin, oldPassword)) {
            setPassword(LoginPasswordCoder.encode(newLogin, newPassword));
        }
        Logger.log("Login and password updated!");
    }

    private void setPassword(String logPass) {
        try {
            configuration.setProperty("hashedPass", StringCipher.hashString(logPass, SALT, 50, 256));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Logger.log(e);
        }
    }

    /**
     * Put or set property
     */
    public void put(String key, Object value) {
        configuration.setProperty(key, value);
    }

    /**
     * Get property
     */
    public Object get(String key) {
        return configuration.getProperty(key);
    }

    /**
     * Remove property
     */
    public void remove(String key) {
        configuration.clearProperty(key);
    }
}
