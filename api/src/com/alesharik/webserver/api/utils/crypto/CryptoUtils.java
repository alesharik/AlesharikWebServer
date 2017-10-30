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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This class contains some utilities for cryptography
 */
@ThreadSafe
@UtilityClass
public class CryptoUtils {
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = ThreadLocal.withInitial(CryptoUtils::newStrongSecureRandom);

    /**
     * Creates new 8-byte salt
     *
     * @return salt bytes
     */
    @Nonnull
    public static byte[] createSalt() {
        byte[] salt = new byte[8];
        SECURE_RANDOM.get().nextBytes(salt);
        return salt;
    }

    /**
     * Generate random alphanumeric string
     *
     * @param size string size
     * @return new random string
     * @throws IllegalArgumentException if size <= 0
     */
    @Nonnull
    public static String generateRandomString(int size) {
        if(size <= 0)
            throw new IllegalArgumentException("Size must be > 0");
        return RandomStringUtils.random(size, 0, 0, true, true, null, SECURE_RANDOM.get());
    }

    /**
     * Creates new {@link SecureRandom} with random-generated seed
     *
     * @return
     */
    @Nonnull
    public static SecureRandom newStrongSecureRandom() {
        Random random = new Random();

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(random.nextLong());

        return secureRandom;
    }
}
