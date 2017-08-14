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

package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
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
