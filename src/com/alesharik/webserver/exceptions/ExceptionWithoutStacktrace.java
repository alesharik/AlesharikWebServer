package com.alesharik.webserver.exceptions;

/**
 * This class extends {@link Exception} and provide creating exceptions without creating stacktrace
 */
public class ExceptionWithoutStacktrace extends Exception {
    public ExceptionWithoutStacktrace() {
        super("", null, true, false);
    }

    public ExceptionWithoutStacktrace(String message) {
        super(message, null, true, false);
    }

    public ExceptionWithoutStacktrace(String message, Throwable cause) {
        super(message, cause, true, false);
    }

    public ExceptionWithoutStacktrace(Throwable cause) {
        super("", cause, true, false);
    }
}

