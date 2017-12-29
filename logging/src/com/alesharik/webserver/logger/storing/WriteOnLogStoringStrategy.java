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

package com.alesharik.webserver.logger.storing;

import com.alesharik.webserver.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class WriteOnLogStoringStrategy extends StoringStrategy {
    private BufferedWriter writer;

    public WriteOnLogStoringStrategy(File file) {
        super(file);
    }

    @Override
    public void open() throws IOException {
        checkFile();
        writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
    }

    @Override
    public void publish(String prefix, String message) {
        try {
            writer.write(prefix + ": " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
