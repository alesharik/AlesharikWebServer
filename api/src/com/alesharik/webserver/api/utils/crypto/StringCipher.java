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

package com.alesharik.webserver.api.utils.crypto;

import com.alesharik.webserver.exceptions.error.UnexpectedBehaviorError;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.utils.Charsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * This class used for encode and decode string with DESede key. If you need to encode and decode byte[] use Base64 to convert
 * byte[] to String
 */
@UtilityClass
@Deprecated
@ThreadSafe
public final class StringCipher {
    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private static SecretKeyFactory SECRET_KEY_FACTORY;
    private static ThreadLocal<Cipher> cipher;

    static {
        try {
            SECRET_KEY_FACTORY = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            cipher = ThreadLocal.withInitial(() -> {
                try {
                    return Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
                    throw new UnexpectedBehaviorError(e1);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new UnexpectedBehaviorError(e);
        }
    }

    /**
     * Encrypt string with specific key <br>
     * WARNING! Key length must be 24!
     *
     * @param data string to encrypt
     * @param key  encryption key, used if has no provided secret key
     * @param keys secret key, in this case key can be null
     * @return encrypted base64 string without \r\n
     */
    @Nonnull
    public static String encrypt(@Nonnull String data, @Nullable String key, @Nullable SecretKey... keys) throws InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        SecretKey secretKey = getSecretKey(key, keys);
        cipher.get().init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] rawData = data.getBytes(UNICODE_FORMAT);
        return Base64Utils.encodeToString(cipher.get().doFinal(rawData), false);
    }

    /**
     * Decrypt string
     * WARNING! Key length must be length 24!
     *
     * @param base64 base64 string with no \r\n
     * @param key    encryption key, used if has no provided secret key
     * @param keys   secret key, in this case key can be null
     * @return decrypted normal string
     */
    @Nonnull
    public static String decrypt(@Nonnull String base64, @Nullable String key, @Nullable SecretKey... keys) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secretKey = getSecretKey(key, keys);
        cipher.get().init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.get().doFinal(Base64Utils.decodeFast(base64)), Charsets.UTF8_CHARSET);
    }

    @Nonnull
    private static SecretKey getSecretKey(@Nullable String key, @Nullable SecretKey[] keys) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
        SecretKey secretKey;
        if(keys.length <= 0) {
            secretKey = generateKey(key);
        } else if(keys.length == 1) {
            secretKey = keys[0];
        } else {
            throw new IllegalArgumentException("Maximum one key!");
        }
        return secretKey;
    }

    /**
     * Generate key form string
     *
     * @param key string length must length be 24
     * @return generated secret key
     * @throws IllegalArgumentException if key length not 24
     */
    @Nonnull
    public static SecretKey generateKey(@Nonnull String key) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
        if(key.length() != 24) {
            throw new IllegalArgumentException("Key length must be 24!");
        }
        byte[] keyBytes = key.getBytes(UNICODE_FORMAT);
        KeySpec keySpec = new DESedeKeySpec(keyBytes);
        return SECRET_KEY_FACTORY.generateSecret(keySpec);
    }

    /**
     * Hash string with PBKDF2WithHmacSHA512 algorithm
     *
     * @param string     string to hash
     * @param salt       32 byte random salt
     * @param iterations iterations on algorithm
     * @param keyLength  length of key, by default set 256
     */
    @Nonnull//TODO refactor
    @SneakyThrows
    public static byte[] hashString(@Nonnull String string, @Nonnull byte[] salt, int iterations, int keyLength) throws InvalidKeySpecException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(string.toCharArray(), salt, iterations, keyLength);
        SecretKey secretKey = skf.generateSecret(spec);
        return secretKey.getEncoded();
    }
}
