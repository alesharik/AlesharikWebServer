package com.alesharik.webserver.js.execution;

import com.alesharik.webserver.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Listen the JavaScript input code
 */
@FunctionalInterface
public interface JavaScriptInputListener {
    void listen(String str);

    default void listenFile(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            listen(bufferedReader.readLine());
        } catch (IOException e) {
            Logger.log(e);
        }
    }
}
