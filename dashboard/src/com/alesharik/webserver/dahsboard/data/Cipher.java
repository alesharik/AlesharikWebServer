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

package com.alesharik.webserver.dahsboard.data;

import com.alesharik.webserver.api.utils.crypto.Hasher;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

@UtilityClass
class Cipher {
    private static final String ALGORITHM = "RSA";
    private static final byte[] SALT = "Potassium ferricyanide".getBytes(StandardCharsets.UTF_16);

    @SneakyThrows(NoSuchAlgorithmException.class)
    public static KeyPair generateKeyPair(String password) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        SecureRandom random = new SecureRandom();
        random.setSeed(password.hashCode());
        generator.initialize(512, random);
        return generator.generateKeyPair();
    }

    @SneakyThrows({NoSuchPaddingException.class, NoSuchAlgorithmException.class, UnsupportedOperationException.class})
    public static byte[] decrypt(byte[] data, PrivateKey key, String password) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
        cipher.init(javax.crypto.Cipher.SECRET_KEY, key);
        byte[] decrypted = cipher.doFinal(data);
        byte[] salt = salt(password);
        for(int i = 0; i < salt.length; i++) {
            if(salt[i] != decrypted[i])
                throw new InvalidKeyException("Password salt mismatch");
        }
        byte[] ret = new byte[decrypted.length - salt.length];
        System.arraycopy(decrypted, salt.length, ret, 0, ret.length);
        return ret;
    }

    @SneakyThrows({NoSuchPaddingException.class, NoSuchAlgorithmException.class, UnsupportedOperationException.class})
    public static byte[] encrypt(PublicKey key, String password, byte[] data) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
        cipher.init(javax.crypto.Cipher.PUBLIC_KEY, key);

        byte[] salt = salt(password);
        cipher.update(salt);

        return cipher.doFinal(data);
    }

    private static byte[] salt(String password) {
        return Hasher.SHA512(password.getBytes(StandardCharsets.UTF_16), SALT);
    }
}
