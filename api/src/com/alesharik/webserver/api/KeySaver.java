package com.alesharik.webserver.api;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * This class used for store keys in files. This class simply save and load the key, not secure it!
 *///FIXME
public final class KeySaver {
    private KeySaver() {
    }

    /**
     * Save key to passed file. This method throw exception on file missing!
     *
     * @param key  key to save
     * @param file the file
     * @throws FileNotFoundException then method can't find file
     */
    public static void saveKeyToFile(SecretKey key, File file) throws IOException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(file);

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
    public static SecretKey loadKeyFromFile(File file, String algorithm) throws IOException {
        Utils.notNullAndEmpty(algorithm);
        Objects.requireNonNull(file);

        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }
        return new SecretKeySpec(Files.readAllBytes(file.toPath()), algorithm);
    }
}
