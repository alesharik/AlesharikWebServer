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
