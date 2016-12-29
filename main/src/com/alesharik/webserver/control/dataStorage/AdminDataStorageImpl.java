package com.alesharik.webserver.control.dataStorage;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.control.AdminDataStorage;
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
import org.glassfish.grizzly.utils.Charsets;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * This class used ONLY in AlesharikWebServer. Do not use it!
 */
@Prefixes(value = {"[ServerControl]", "[AdminDataStorage]"})
public final class AdminDataStorageImpl implements AdminDataStorage {
    private static final String ADMIN_DATA_FILE = "adminData.dat";
    private static final String ADMIN_KEY_FILE = "adminKey.key";
    private byte[] salt = new byte[32];
    private String adminKey;
    private Configuration configuration;

    private String serverKey;
    private File adminKeyFile;

    /**
     * Initialize new {@link AdminDataStorageImpl}
     *
     * @param key encryption key. Length of key must be equals 24!
     */
    public AdminDataStorageImpl(String key) throws ConfigurationException, IOException {
        serverKey = key;

        File adminDataFile = new File(Main.USER_DIR + "/" + ADMIN_DATA_FILE);
        if(!adminDataFile.exists()) {
            if(!adminDataFile.createNewFile()) {
                Logger.log("Oops! Problem with creating holder file: " + adminDataFile);
                throw new IOException("Can't create file: " + adminDataFile);
            }
        }
        adminKeyFile = new File(Main.USER_DIR + "/" + ADMIN_KEY_FILE);
        if(!adminKeyFile.exists()) {
            if(!adminKeyFile.createNewFile()) {
                Logger.log("Oops! Problem with creating holder file: " + adminKeyFile);
                throw new IOException("Can't create file: " + adminKeyFile);
            }
        }

        loadSalt(adminKeyFile);
        try {
            loadAdminKey(key, adminKeyFile);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e) {
            Logger.log(e);
        }

        PropertiesBuilderParameters properties = new Parameters().properties();
        FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(properties
                        .setFileName(ADMIN_DATA_FILE)
                        .setIOFactory(new EncryptedIOFactory(adminKey))
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';')));
        fileBasedConfigurationBuilder.setAutoSave(true);
        configuration = fileBasedConfigurationBuilder.getConfiguration();

        Logger.log("AdminDataStorageImpl successfully initialized!");
    }

    /**
     * Load or create salt
     */
    private void loadSalt(File adminKeyFile) throws IOException {
        BufferedReader bufferedReader = Files.newBufferedReader(adminKeyFile.toPath());
        String saltLine = bufferedReader.readLine();
        if(saltLine == null) {
            salt = createSalt(adminKeyFile);
            return;
        }
        salt = Base64Utils.decodeFast(saltLine);
        bufferedReader.close();
    }

    private void loadAdminKey(String serverKey, File adminKeyFile) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        BufferedReader bufferedReader = Files.newBufferedReader(adminKeyFile.toPath());
        bufferedReader.readLine();
        String key = bufferedReader.readLine();

        if(key == null) {
            adminKey = setAdminKey(serverKey, adminKeyFile, "admin", "admin");
            return;
        }
        adminKey = StringCipher.decrypt(key, serverKey);

        bufferedReader.close();
    }

    private String setAdminKey(String serverKey, File adminKeyFile, String login, String password) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        List<String> lines = Files.readAllLines(adminKeyFile.toPath());
        if(lines.size() <= 0) {
            throw new IllegalStateException("Oops!");
        }
        String salt = lines.get(0);

        String key = salt + "\n" + StringCipher.encrypt(generateAdminKey(login, password), serverKey);
        Files.write(adminKeyFile.toPath(), key.getBytes(Charsets.UTF8_CHARSET));
        return key;
    }

    /**
     * Write salt to file(remove all data!)
     */
    private byte[] createSalt(File adminKeyFile) throws IOException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);
        Files.write(adminKeyFile.toPath(), Base64Utils.encodeToByte(salt, false));
        return salt;
    }

    public boolean check(String login, String password) {
        return check(LoginPasswordCoder.encode(login, password));
    }

    public boolean check(String logpass) {
        return adminKey.equals(generateAdminKey(logpass));
    }

    public void updateLoginPassword(String oldLogin, String oldPassword, String newLogin, String newPassword) {
        if(check(oldLogin, oldPassword)) {
            try {
                adminKey = setAdminKey(serverKey, adminKeyFile, newLogin, newPassword);
            } catch (IOException | IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
                Logger.log(e);
                return;
            }
        }
        Logger.log("Login and password updated!");
    }

    private String generateAdminKey(String login, String password) {
        return generateAdminKey(LoginPasswordCoder.encode(login, password));
    }

    private String generateAdminKey(String logPass) {
        try {
            byte[] key = new byte[24];
            System.arraycopy(StringCipher.hashString(logPass, salt, 50, 256), 0, key, 0, 24);
            return Base64Utils.encodeToString(key, false);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Logger.log(e);
        }
        return "";
    }

    public void put(String key, Object value) {
        configuration.setProperty(key, value);
    }

    public Object get(String key) {
        return configuration.getProperty(key);
    }

    public void remove(String key) {
        configuration.clearProperty(key);
    }

    public boolean contains(String key) {
        return configuration.containsKey(key);
    }
}
