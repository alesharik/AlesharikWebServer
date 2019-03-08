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

package com.alesharik.webserver.benchmark;

import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;
import java.io.PrintStream;

abstract class AbstractOutputFormat implements OutputFormat {
    private final VerboseMode verbose;
    protected final PrintStream out;

    public AbstractOutputFormat(PrintStream out, VerboseMode verbose) {
        this.out = out;
        this.verbose = verbose;
    }

    @Override
    public void print(String s) {
        out.print(s);
    }

    @Override
    public void println(String s) {
        out.println(s);
    }

    @Override
    public void flush() {
        out.flush();
    }

    @Override
    public void verbosePrintln(String s) {
        if(verbose == VerboseMode.EXTRA)
            out.println(s);
    }

    @Override
    public void write(int b) {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() {
    }
}
