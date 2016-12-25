package com.alesharik.webserver.logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class format message according to the scheme: <br>
 * [Date: + current date + ] + message + \r\n
 */
public class LoggerFormatter extends Formatter {

    private final Date dat = new Date();

    @Override
    public synchronized String format(LogRecord record) {
        dat.setTime(record.getMillis());
        return "[Date: " + dat.toString() + "]" + record.getMessage() + "\r\n";
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        return format(record);
    }
}
