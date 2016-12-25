package com.alesharik.webserver.generators;

import org.glassfish.grizzly.http.server.Request;

public interface ErrorPageGenerator {
    String generate(Request request, int status, String reasonPhrase, String description, Throwable exception);
}
