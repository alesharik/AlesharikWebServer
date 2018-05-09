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

package com.alesharik.webserver.base.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Use it when a dev does something really wrong
 */
public final class DevError extends Error {
    private static final long serialVersionUID = 4741623557743824142L;
    private final String message;
    private final String description;
    private final String clazz;

    public DevError(String message, String description, Class<?> clazz) {
        this(message, description, clazz.getCanonicalName());
    }

    public DevError(String message, String description, String clazz) {
        this.message = message;
        this.description = description;
        this.clazz = clazz;
    }

    List<String> renderMessage() {
        List<String> list = new ArrayList<>();

        list.add("====================DEV ERROR====================");
        list.add("CRITICAL ERROR DETECTED! FILE A BUG REPORT TO THE DEVELOPERS!");
        list.add("====================ERROR====================");
        list.add(message);
        list.add("====================DESCRIPTION====================");
        list.add(description);
        list.add("====================CLASS====================");
        list.add(clazz);
        list.add("====================ERROR END====================");

        return list;
    }

    @Override
    public String getMessage() {
        return renderMessage().stream().reduce((s, s2) -> s + '\n' + s2).orElseThrow(() -> new UnknownError("DevError render error. Server is corrupted. Re-installation is required!")) + '\n';
    }
}
