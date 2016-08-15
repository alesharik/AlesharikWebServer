package com.alesharik.webserver.generators;

import com.alesharik.webserver.exceptions.KeyExistsException;
import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;

import java.util.HashMap;

/**
 * This error page generator use modules as additional error page providers
 */
public final class ModularErrorPageGenerator implements ErrorPageGenerator {
    private final AdvancedErrorPageGenerator pageGenerator;
    private final HashMap<Integer, ErrorPageGenerator> errorPageGenerators = new HashMap<>();

    public ModularErrorPageGenerator(FileManager fileManager) {
        this.pageGenerator = new AdvancedErrorPageGenerator(fileManager);
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        if(errorPageGenerators.containsKey(status)) {
            return errorPageGenerators.get(status).generate(request, status, reasonPhrase, description, exception);
        } else {
            return pageGenerator.generate(request, status, reasonPhrase, description, exception);
        }
    }

    /**
     * Add constructor
     *
     * @throws KeyExistsException if status is busy
     */
    public void addConstructor(ErrorPageConstructor constructor) {
        if(hasConstructorOn(constructor.getStatus())) {
            throw new KeyExistsException();
        }

        errorPageGenerators.put(constructor.getStatus(), constructor);
    }

    /**
     * Check if status is busy
     */
    public boolean hasConstructorOn(int status) {
        return errorPageGenerators.containsKey(status);
    }

    /**
     * Set free current status
     */
    public void clearStatus(int status) {
        errorPageGenerators.remove(status);
    }

    public void setupServerAccessManagerBuilder(ServerAccessManagerBuilder builder) {
        builder.setErrorPageGenerator(this);
    }
}
