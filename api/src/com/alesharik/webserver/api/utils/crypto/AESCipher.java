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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

@Level("Crypto")
@Prefixes("[AES]")
@UtilityClass
public class AESCipher {
    private static volatile boolean ENABLED;
    private static final SecretKeyFactory SECRET_KEY_FACTORY;

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("Crypto");

        boolean enabled;
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            enabled = true;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("AES(PBKDF2WithHmacSHA256) algorithm not found");

            enabled = false;
        }

        SECRET_KEY_FACTORY = secretKeyFactory;

        if(enabled) {
            ENABLED = true;

            System.out.println("Test run initiated!");
            SecretKey secretKey = generateNewSecretKey(CryptoUtils.createSalt(), 65536, 256);
            String test = "This is a test AES string";

            System.out.println("Test string is \"" + test + '"');

            try {
                byte[][] encrypted = encrypt(test.getBytes(Charset.forName("UTF-8")), secretKey);
                String decrypted = new String(decrypt(encrypted[0], secretKey, encrypted[1]), Charset.forName("UTF-8"));
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
     * @param password   the password
     * @param salt       the salt
     * @param iterations the iteration count.
     * @param keyLength  the to-be-derived key length.
     * @return AES {@link SecretKey} instance
     * @throws RuntimeException if key is invalid
     */
    @Nonnull
    public static SecretKey getSecretKey(String password, byte[] salt, int iterations, int keyLength) {
        if(!ENABLED)
            throw new IllegalArgumentException("AES encryption is not supported!");

        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        try {
            SecretKey secretKey = SECRET_KEY_FACTORY.generateSecret(keySpec);
            return new SecretKeySpec(secretKey.getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create new random AES secret key
     *
     * @param salt       key salt
     * @param iterations the iteration count(65536 should be good)
     * @param keyLength  key length(256 - recommended)
     * @return new secret key
     */
    @Nonnull
    public static SecretKey generateNewSecretKey(byte[] salt, int iterations, int keyLength) {
        return getSecretKey(CryptoUtils.generateRandomString(keyLength), salt, iterations, keyLength);
    }

    /**
     * Encrypt data with AES key
     *
     * @param data      the data
     * @param secretKey the secret key
     * @return encrypted data(index - 0) and IV(index - 1)
     * @throws IllegalArgumentException if key is invalid
     */
    @Nonnull
    public static byte[][] encrypt(@Nonnull byte[] data, @Nonnull SecretKey secretKey) {
        if(!ENABLED)
            throw new IllegalArgumentException("AES encryption is not supported!");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            AlgorithmParameters parameters = cipher.getParameters();
            byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();
            return new byte[][]{cipher.doFinal(data), iv};
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidParameterSpecException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Decrypt AES-encrypted data
     *
     * @param encrypted the data
     * @param secretKey the secret key
     * @param iv        the iv from encryption
     * @return decrypted data
     * @throws IllegalArgumentException if key is invalid
     */
    @Nonnull
    public static byte[] decrypt(@Nonnull byte[] encrypted, @Nonnull SecretKey secretKey, @Nonnull byte[] iv) {
        if(!ENABLED)
            throw new IllegalArgumentException("AES encryption is not supported!");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
