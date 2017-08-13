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

package com.alesharik.webserver.control.data.storage;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.module.security.SecuredStoreAccessController;
import com.alesharik.webserver.module.security.SecuredStoreModule;
import lombok.SneakyThrows;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.utils.Charsets;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
public final class AdminDataStorageImpl implements AdminDataStorage, SecuredStoreAccessController {
    private static SecretKey SECRET_KEY;

    static {
        try {
            SECRET_KEY = StringCipher.generateKey("QZ2VFvxBh2WuYkfeCHN3Sshu");
        } catch (InvalidKeySpecException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private byte[] salt = new byte[32];
    private String adminKey;
    private Configuration configuration;
    private SecuredStoreModule securedStoreModule;

    private String serverKey;
    private File adminKeyFile;

    public AdminDataStorageImpl() {
    }


    @SneakyThrows
    @Override
    public void parse(@Nullable Element configNode) {
        File adminKey = XmlHelper.getFile("adminKeyFile", configNode, true);
        File adminData = XmlHelper.getFile("adminDataFile", configNode, true);
        if(!adminKey.exists()) {
            try {
                if(!adminKey.createNewFile()) {
                    throw new ConfigurationParseError("Can't create file: " + adminKey);
                }
            } catch (IOException e) {
                throw new ConfigurationParseError(e);
            }
        }
        if(!adminData.exists()) {
            try {
                if(!adminData.createNewFile()) {
                    throw new ConfigurationParseError("Can't create file: " + adminData);
                }
            } catch (IOException e) {
                throw new ConfigurationParseError(e);
            }
        }
        adminKeyFile = adminKey;
        try {
            loadSalt(adminKeyFile);
        } catch (IOException e) {
            throw new ConfigurationParseError(e);
        }

        securedStoreModule = XmlHelper.getSecuredStore("securedStore", configNode, true);
        securedStoreModule.storeString(this, "adminKey");

        String key;
        try {
            key = securedStoreModule.readString("adminKey");
        } catch (IOException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
            throw new ConfigurationParseError(e);
        }
        if(key.isEmpty()) {
            key = Utils.getRandomString(24);
            try {
                securedStoreModule.writeString("adminKey", key);
            } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException | IOException e) {
                throw new ConfigurationParseError(e);
            }
        }
        serverKey = key;
        try {
            loadAdminKey(key, adminKeyFile);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException | IOException e) {
            throw new ConfigurationParseError(e);
        }

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(adminData.getName())
                        .setIOFactory(new EncryptedIOFactory(this.adminKey))
                        .setThrowExceptionOnMissing(true)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(';')));
        fileBasedConfigurationBuilder.setAutoSave(true);
        try {
            configuration = fileBasedConfigurationBuilder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new ConfigurationParseError(e);
        }
    }

    @Override
    public void reload(@Nullable Element configNode) {
        parse(configNode);
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

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void shutdownNow() {
    }

    @Nonnull
    @Override
    public String getName() {
        return "admin-data-storage";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }

    @Override
    public boolean grantAccess(Class<?> clazz) {
        return clazz.equals(this.getClass());
    }

    @Override
    public SecretKey passwordKey() {
        return SECRET_KEY;
    }
}
