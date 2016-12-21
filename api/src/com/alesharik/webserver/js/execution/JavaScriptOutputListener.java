package com.alesharik.webserver.js.execution;

/**
 * Listen JavaScript output
 */
@FunctionalInterface
public interface JavaScriptOutputListener {
    void listen(String str);
}
