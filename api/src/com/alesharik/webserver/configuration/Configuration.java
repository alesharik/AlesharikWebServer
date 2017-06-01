package com.alesharik.webserver.configuration;

import org.w3c.dom.Element;

/**
 * The class, implementing this interface, will use for load all AlesharikWebServer XML configuration.
 * You can see AlesharikWebServer XML configuration documentation <a href="https://github.com/alesharik/AlesharikWebServer/wiki/XML-configuration">here</a>.
 * This class works only with <code>modules</code> and <code>main</code> parts of xml.
 */
public interface Configuration {
    void parseModules(Element modules);

    void parseMain(Element main);

    Module getModuleByName(String name);

    /**
     * Executes after fatal error. All modules must be stopped by calling {@link Module#shutdownNow()} method. All throwables
     * must not interrupt other modules stopping procedure
     */
    void shutdownNow();

    /**
     * Executes after exit code received. All modules must be stopped by calling {@link Module#shutdown()} method.
     * All throwables must not interrupt other modules stopping procedure
     */
    void shutdown();
}
