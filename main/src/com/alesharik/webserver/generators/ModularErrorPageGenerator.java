package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import org.glassfish.grizzly.http.server.Request;

import java.util.List;

/**
 * This error page generator use modules as additional error page providers
 */
public final class ModularErrorPageGenerator implements ErrorPageGenerator {
    private final ErrorPageConstructors constructors;

    public ModularErrorPageGenerator(FileManager fileManager) {
        constructors = new ErrorPageConstructors();
        constructors.addConstructor(new BasicErrorPageConstructor());
        constructors.addConstructor(new FileBasedErrorPageConstructor(fileManager));
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        return constructors.getConstructor(status)
                .orElseThrow(() -> new RuntimeException("Page constructor not found"))
                .generate(request, status, reasonPhrase, description, exception);
    }

    //TODO remove
    @Deprecated
    public void setupServerAccessManagerBuilder(ServerAccessManagerBuilder builder) {
        builder.setErrorPageGenerator(this);
    }

    @Override
    public void addErrorPageConstructor(ErrorPageConstructor constructor) {
        constructors.addConstructor(constructor);
    }

    @Override
    public void removeErrorPageConstructor(ErrorPageConstructor constructor) {
        constructors.removeErrorPageConstructor(constructor);
    }

    @Override
    public boolean containsErrorPageConstructor(ErrorPageConstructor constructor) {
        return constructor != null && constructors.containsConstructor(constructor);
    }

    @Override
    public List<ErrorPageConstructor> getErrorPageConstructorsForStatus(int status) {
        return constructors.constructors(status);
    }

    @Override
    public void setDefaultErrorPageConstructor(ErrorPageConstructor errorPageConstructor, int status) {
        constructors.setDefault(errorPageConstructor, status);
    }
}
