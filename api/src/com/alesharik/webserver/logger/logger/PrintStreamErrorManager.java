package com.alesharik.webserver.logger.logger;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.logging.ErrorManager;

/**
 * Use specified {@link PrintStream} for write messages
 */
public final class PrintStreamErrorManager extends ErrorManager {
    private final PrintStream printStream;

    public PrintStreamErrorManager(@Nonnull PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Do not ignore subsequent calls. Message starts with <code>[LOGGER][ERROR]</code>
     *
     * @param msg  a message (may be <code>null</code>)
     * @param ex   a exception (may be <code>null</code>)
     * @param code an error code defined in ErrorManager
     */
    @Override
    public synchronized void error(String msg, Exception ex, int code) {
        String text = "[LOGGER][ERROR]: " + code;
        if(msg != null) {
            text = text + ": " + msg;
        }
        printStream.println(text);
        if(ex != null) {
            ex.printStackTrace(printStream);
        }
    }
}
