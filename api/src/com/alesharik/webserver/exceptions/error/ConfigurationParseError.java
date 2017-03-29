package com.alesharik.webserver.exceptions.error;

/**
 * Error in {@link com.alesharik.webserver.configuration.Configurator} in api section
 */
public class ConfigurationParseError extends Error {
    public ConfigurationParseError() {
    }

    public ConfigurationParseError(String message) {
        super(message);
    }

    public ConfigurationParseError(Throwable cause) {
        super(cause);
    }
}
