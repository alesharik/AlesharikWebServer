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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Storing strategy used by {@link com.alesharik.webserver.logger.NamedLogger} for storing messages to file
 */
public abstract class StoringStrategy {
    /**
     * File, where log is stored
     */
    protected final File file;

    protected StoringStrategy(File file) {
        this.file = file;
    }

    public abstract void open() throws IOException;

    public abstract void close() throws IOException;

    /**
     * Call after log with message. If logger tries to log throwable, all lines will be passed separately.
     * You can do long operations in this method
     *
     * @param prefix  compete prefix. Starts with [Logger][name]
     * @param message the message
     */
    public abstract void publish(@Nonnull String prefix, @Nonnull String message);

    /**
     * Check file for existence and correctness
     *
     * @throws IOException if anything unusual happen
     */
    protected void checkFile() throws IOException {
        if(!file.exists())
            if(!file.createNewFile())
                throw new IOException("Cannot create new file!");
            else if(file.isDirectory())
                throw new IOException("Folder not expected!");
            else if(!file.canWrite())
                throw new IOException("Don't have permissions to write into file!");
    }
}
