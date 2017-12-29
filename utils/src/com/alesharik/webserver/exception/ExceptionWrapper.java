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

package com.alesharik.webserver.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class ExceptionWrapper extends RuntimeException {
    protected final Exception exception;

    protected ExceptionWrapper(Exception exception) {
        this.exception = exception;
    }

    protected ExceptionWrapper(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    protected ExceptionWrapper(String message, Exception cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.exception = cause;
    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return exception.getLocalizedMessage();
    }

    @Override
    public Exception getCause() {
        return exception;
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return exception.initCause(cause);
    }

    @Override
    public String toString() {
        return exception.toString();
    }

    @Override
    public void printStackTrace() {
        exception.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        exception.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        exception.printStackTrace(s);
    }

    @Override
    public Throwable fillInStackTrace() {
        return exception.fillInStackTrace();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return exception.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        exception.setStackTrace(stackTrace);
    }
}
