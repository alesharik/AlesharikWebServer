package com.alesharik.webserver.control.dataHolding;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.logger.Logger;
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
 * This class used for hold data of admin
 */
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
        File adminDadaFile = new File(Main.USER_DIR + "/" + ADMIN_DATA_FILE);
        if(!adminDadaFile.exists()) {
            adminDadaFile.createNewFile();
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
            String base64HashedLogPass = Base64Utils.encodeToString(StringCipher.hashString(logpass, SALT, 2, 256), false);
            return configuration.getString("hashedPass").equals(base64HashedLogPass);
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
    }

    private void setPassword(String logPass) {
        try {
            configuration.setProperty("hashedPass", Base64Utils.encodeToString(StringCipher.hashString(logPass, SALT, 2, 256), false));
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
