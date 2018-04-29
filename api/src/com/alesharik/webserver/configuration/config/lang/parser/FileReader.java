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

package com.alesharik.webserver.configuration.config.lang.parser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * File reader wraps all interaction with file system
 */
public interface FileReader {
    /**
     * @param path relative path
     * @return
     */
    List<String> readFile(Path path);

    default boolean isFile(File file) {
        return file.isFile();
    }

    default boolean exists(File file) {
        return file.exists();
    }

    default boolean canRead(File file) {
        return file.canRead();
    }

    default boolean canExecute(File file) {
        return file.canExecute();
    }
}
