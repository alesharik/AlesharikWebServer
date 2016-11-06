package com.alesharik.webserver.api;

import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class KeySaverTest {
    @Test
    public void saveKeyToFile() throws Exception {
        File file = File.createTempFile("AWSPrefix", null);
        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException();
            }
        }
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        KeySaver.saveKeyToFile(secretKey, file);
        assertTrue(secretKey.equals(KeySaver.loadKeyFromFile(file, "AES")));
    }

    @Test
    public void exceptions() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();

        File none = new File("");
        try {
            KeySaver.saveKeyToFile(null, none);
            fail();
        } catch (IOException | NullPointerException e) {
            //OK
        }
        try {
            KeySaver.saveKeyToFile(secretKey, none);
            fail();
        } catch (IOException | NullPointerException e) {
            //OK
        }
        try {
            KeySaver.loadKeyFromFile(none, "");
            fail();
        } catch (IOException | IllegalArgumentException e) {
            //OK
        }
        try {
            KeySaver.loadKeyFromFile(none, "AES");
            fail();
        } catch (IOException e) {
            //OK
        }
    }
}