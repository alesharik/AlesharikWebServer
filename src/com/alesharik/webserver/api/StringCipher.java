package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.http.util.Base64Utils;

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
public final class StringCipher {
    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private static SecretKeyFactory SECRET_KEY_FACTORY;
    private static Cipher cipher;

    static {
        try {
            SECRET_KEY_FACTORY = SecretKeyFactory.getInstance(DESEDE_ENCRYPTION_SCHEME);
            cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Logger.log(e);
        }
    }

    private StringCipher() {
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
    public static String encrypt(String data, String key, SecretKey... keys) throws InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        SecretKey secretKey = getSecretKey(key, keys);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] rawData = data.getBytes(UNICODE_FORMAT);
        return Base64Utils.encodeToString(cipher.doFinal(rawData), false);
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
    public static String decrypt(String base64, String key, SecretKey... keys) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secretKey = getSecretKey(key, keys);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64Utils.decodeFast(base64)));
    }

    private static SecretKey getSecretKey(String key, SecretKey[] keys) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
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
    public static SecretKey generateKey(String key) throws InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
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
    public static byte[] hashString(String string, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec spec = new PBEKeySpec(string.toCharArray(), salt, iterations, keyLength);
        SecretKey secretKey = skf.generateSecret(spec);
        return secretKey.getEncoded();
    }
}
