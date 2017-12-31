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

package com.alesharik.webserver.js.execution;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.SneakyThrows;
import org.glassfish.grizzly.utils.Charsets;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the JavaScript nashorn-based engine.
 * Used for execute JavaScript and use Thread api
 * Also as terminal JavaScript utility
 */
public final class JavaScriptEngine {
    private static final NashornScriptEngineFactory ENGINE_FACTORY = new NashornScriptEngineFactory();

    private final ScriptEngine engine;
    private final CopyOnWriteArrayList<JavaScriptInputListener> inputListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<JavaScriptOutputListener> outputListeners = new CopyOnWriteArrayList<>();
    private JavaScriptExceptionHandler exceptionHandler = (e) -> {
    };

    /**
     * Create new nashorn engine with -strict option
     *
     * @param noJava        if it is true, disable all Java api (and Thread api)
     * @param loadThreadApi enable Thread api. Need java enabled!
     */
    public JavaScriptEngine(boolean noJava, boolean loadThreadApi) {
        if(noJava) {
            String[] config = {"-strict", "--no-java"};
            engine = ENGINE_FACTORY.getScriptEngine(config);
        } else {
            String[] config = {"-strict"};
            engine = ENGINE_FACTORY.getScriptEngine(config);
            if(loadThreadApi) {
                loadThreadApi();
            }
        }
        engine.getContext().setWriter(new Writer() {
            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void write(char[] cbuf, int off, int len) {
                stringBuilder.append(cbuf, off, len);
            }

            @Override
            public void flush() {
                outputListeners.forEach(javaScriptOutputListener -> javaScriptOutputListener.listen(stringBuilder.toString()));
                stringBuilder.delete(0, stringBuilder.length());
            }

            @Override
            public void close() {
                //Okay
            }
        });
    }

    @SneakyThrows //Because this lines can't throw any exception
    private void loadThreadApi() {
        engine.eval("var Thread = com.alesharik.webserver.js.execution.js.JSThread;");
        engine.eval("var Mutex = com.alesharik.webserver.js.execution.js.Mutex;");
    }

    /**
     * Load and execute JavaScript file
     *
     * @param file the file
     * @throws FileNotFoundException if file not found
     * @throws ScriptException       if there are any errors along script execution
     * @throws IOException           if anything happens
     */
    public void load(@Nonnull File file) throws IOException, ScriptException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF8_CHARSET)) {
            engine.eval(reader);
            inputListeners.forEach(inputListener -> inputListener.listenFile(file));
        } catch (ScriptException e) {
            exceptionHandler.handle(e);
            throw e;
        }
    }

    /**
     * Execute line(or lines) of code
     *
     * @param code the code
     * @throws ScriptException if there are any errors along script execution
     */
    public void execute(@Nonnull String code) throws ScriptException {
        try {
            engine.eval(code);
            inputListeners.forEach(inputListener -> inputListener.listen(code));
        } catch (ScriptException e) {
            exceptionHandler.handle(e);
            throw e;
        }
    }

    //====================Listeners and Exception Handler====================\\

    public void addInputListener(@Nonnull JavaScriptInputListener inputListener) {
        inputListeners.add(inputListener);
    }

    public void removeInputListener(@Nonnull JavaScriptInputListener listener) {
        inputListeners.remove(listener);
    }

    public boolean containsInputListener(@Nonnull JavaScriptInputListener inputListener) {
        return inputListeners.contains(inputListener);
    }

    public void addOutputListener(@Nonnull JavaScriptOutputListener inputListener) {
        outputListeners.add(inputListener);
    }

    public void removeOutputListener(@Nonnull JavaScriptOutputListener listener) {
        outputListeners.remove(listener);
    }

    public boolean containsOutputListener(@Nonnull JavaScriptOutputListener inputListener) {
        return outputListeners.contains(inputListener);
    }

    @Nonnull
    public JavaScriptExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(@Nonnull JavaScriptExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Start simple JavaScript code executor
     */
    public static void main(String[] args) {
        System.out.println("Started! Use exit to exit");
        JavaScriptEngine javaScriptEngine = new JavaScriptEngine(false, true);
        Scanner scanner = new Scanner(System.in, Charsets.UTF8_CHARSET.name());
        String line;
        while(!(line = scanner.nextLine()).equals("exit")) {
            try {
                javaScriptEngine.execute(line);
            } catch (ScriptException e) {
                System.out.println("OOPS!");
                e.printStackTrace();
            }
        }
        System.out.println("Stopped");
    }
}
