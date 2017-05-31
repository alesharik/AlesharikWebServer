package com.alesharik.webserver.logger.logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Format message according to the scheme: <br>
 * <code>[Date: + message date + ] + message + \n</code>
 */
public final class LoggerDateFormatter extends Formatter {
    /**
     * This date used only for formatting
     */
    private final Date date = new Date();

    @Override
    public synchronized String format(LogRecord record) {
        date.setTime(record.getMillis());
        return "[Date: " + date.toString() + ']' + record.getMessage() + "\n";
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        return format(record);
    }
}
