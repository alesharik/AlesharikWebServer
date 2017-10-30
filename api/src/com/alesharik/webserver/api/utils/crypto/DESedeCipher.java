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

package com.alesharik.webserver.api.utils.crypto;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * This class contains DESede utils
 */
@Level("Crypto")
@Prefixes("[DESede]")
@UtilityClass
public class DESedeCipher {
    private static volatile boolean ENABLED;
    private static final SecretKeyFactory SECRET_KEY_FACTORY;

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("Crypto");

        boolean enabled;
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("DESede");
            enabled = true;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("DESede algorithm not found");

            enabled = false;
        }

        SECRET_KEY_FACTORY = secretKeyFactory;

        if(enabled) {
            ENABLED = true;

            System.out.println("Test run initiated!");
            SecretKey secretKey = generateNewSecretKey();
            String test = "This is a test DESede string";

            System.out.println("Test string is \"" + test + '"');

            try {
                byte[] encrypted = encrypt(test.getBytes(Charset.forName("UTF-8")), secretKey);
                String decrypted = new String(decrypt(encrypted, secretKey), Charset.forName("UTF-8"));
                System.out.println("Test run results: original - " + test + ", decrypted - " + decrypted);
                if(!test.equals(decrypted)) {
                    System.out.println("Test run failed! Strings are different!");
                    enabled = false;
                } else
                    enabled = true;
            } catch (Exception e) {
                System.out.println("Test run failed with exception!");
                e.printStackTrace(System.out);
                enabled = false;
            }

            System.out.println("Test run finished!");
        }

        ENABLED = enabled;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    /**
     * Generate new {@link SecretKey} from it's data
     *
     * @param password the password
     * @return DESede {@link SecretKey} instance
     * @throws RuntimeException         if key spec is invalid
     * @throws IllegalArgumentException if key is invalid
     */
    @Nonnull
    public static SecretKey getSecretKey(@Nonnull byte[] password) {
        if(!ENABLED)
            throw new IllegalArgumentException("DESede encryption is not supported!");
        if(password.length != 24)
            throw new IllegalArgumentException("Password length must be 24!");

        try {
            KeySpec keySpec = new DESedeKeySpec(password);
            return SECRET_KEY_FACTORY.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Create new random DESede secret key
     *
     * @return new secret key
     */
    @Nonnull
    public static SecretKey generateNewSecretKey() {
        return getSecretKey(CryptoUtils.generateRandomBytes(24));
    }

    /**
     * Encrypt data with DESede key
     *
     * @param data      the data
     * @param secretKey the secret key
     * @return encrypted data(index - 0) and IV(index - 1)
     * @throws IllegalArgumentException if key is invalid
     */
    @Nonnull
    public static byte[] encrypt(@Nonnull byte[] data, @Nonnull SecretKey secretKey) {
        if(!ENABLED)
            throw new IllegalArgumentException("DESede encryption is not supported!");
        try {
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Decrypt DESede-encrypted data
     *
     * @param encrypted the data
     * @param secretKey the secret key
     * @return decrypted data
     * @throws IllegalArgumentException if key is invalid
     */
    @Nonnull
    public static byte[] decrypt(@Nonnull byte[] encrypted, @Nonnull SecretKey secretKey) {
        if(!ENABLED)
            throw new IllegalArgumentException("DESede encryption is not supported!");
        try {
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
