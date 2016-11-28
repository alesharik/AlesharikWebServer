package com.alesharik.webserver.js.execution;

import javax.script.ScriptException;

/**
 * Handle all exception
 */
@FunctionalInterface
public interface JavaScriptExceptionHandler {
    void handle(ScriptException e) throws ScriptException;
}
