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

package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.config.lang.parser.FileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class FileReaderImpl implements FileReader {
    @Override
    public List<String> readFile(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new FileReaderException(e);
        }
    }

    public static final class FileReaderException extends RuntimeException {
        private static final long serialVersionUID = 4044822939932847333L;

        public FileReaderException(Throwable cause) {
            super(cause);
        }
    }
}
