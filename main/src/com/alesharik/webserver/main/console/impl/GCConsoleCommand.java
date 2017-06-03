package com.alesharik.webserver.main.console.impl;

import com.alesharik.webserver.main.console.ConsoleCommand;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public class GCConsoleCommand implements ConsoleCommand {
    @Nonnull
    @Override
    public String getName() {
        return "gc";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "execute garbage collection";
    }

    @Override
    public void printHelp(PrintStream printStream) {
        printStream.println("Execute garbage collection");
    }

    @Override
    public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {
        System.gc();
    }
}
