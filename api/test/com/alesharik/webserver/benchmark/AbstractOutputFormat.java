package com.alesharik.webserver.benchmark;

import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;
import java.io.PrintStream;

public abstract class AbstractOutputFormat implements OutputFormat {

    /**
     * Verbose-mode?
     */
    final VerboseMode verbose;
    /**
     * PrintStream to use
     */
    final PrintStream out;

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
        if(verbose == VerboseMode.EXTRA) {
            out.println(s);
        }
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
        // do nothing
    }
}
