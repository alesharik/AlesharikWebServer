package com.alesharik.webserver.api;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class used for store keys at disk
 */
public final class KeyHolder {
    private KeyHolder() {
    }

    /**
     * Save key to passed file <br>
     * WARNING! If file doesn't exists, this throw an exception!
     *
     * @param key  key to save
     * @param file file, who exists
     */
    public static void saveKeyToFile(SecretKey key, File file) throws IOException {
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        Files.write(file.toPath(), key.getEncoded());
    }

    /**
     * Load key from file
     * WARNING! If file doesn't exists, this throw an exception!
     *
     * @param file      file, who exists
     * @param algorithm algorithm of saved key
     * @return saved key
     */
    public static SecretKey loadKeyFromFile(File file, String algorithm) throws IOException {
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        return new SecretKeySpec(Files.readAllBytes(file.toPath()), algorithm);
    }
}
