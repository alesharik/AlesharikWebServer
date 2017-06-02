package com.alesharik.webserver.main.console.impl;

import com.alesharik.webserver.main.Main;
import com.alesharik.webserver.main.console.ConsoleCommand;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public final class ShutdownConsoleCommand implements ConsoleCommand {
    @Nonnull
    @Override
    public String getName() {
        return "shutdown";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "shutdown server";
    }

    @Override
    public void printHelp(PrintStream printStream) {
        printStream.println("Shutdown server correctly. Use this command for shutdown server in normal situations");
    }

    @Override
    public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {
        Main.shutdown();
    }
}
