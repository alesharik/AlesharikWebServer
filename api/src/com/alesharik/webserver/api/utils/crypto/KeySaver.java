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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class used for store keys in files. This class simply save and load the key, not secure it!
 */
@UtilityClass
@ThreadSafe
public final class KeySaver {
    /**
     * Save key to passed file. This method throw exception on file missing!
     *
     * @param key  key to save
     * @param file the file
     * @throws FileNotFoundException then method can't find file
     */
    public static void saveKeyToFile(@Nonnull SecretKey key, @Nonnull File file) throws IOException {
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        Files.write(file.toPath(), key.getEncoded());
    }

    /**
     * Load key from file. This function throw exception on file missing!
     *
     * @param file      the file
     * @param algorithm algorithm of saved key
     * @return saved key
     * @throws FileNotFoundException then method can't find file
     */
    @Nonnull
    public static SecretKey loadKeyFromFile(@Nonnull File file, @Nonnull String algorithm) throws IOException {
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        return new SecretKeySpec(Files.readAllBytes(file.toPath()), algorithm);
    }
}
