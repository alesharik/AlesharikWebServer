package com.alesharik.webserver.main.console.impl;

import com.alesharik.webserver.main.Main;
import com.alesharik.webserver.main.console.ConsoleCommand;

import javax.annotation.Nonnull;
import java.io.PrintStream;

public final class ShutdownNowConsoleCommand implements ConsoleCommand {
    @Nonnull
    @Override
    public String getName() {
        return "shutdownNow";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "shutdown server immediately";
    }

    @Override
    public void printHelp(PrintStream printStream) {
        printStream.println("Shutdown server immediately! For normal server shutdown use \"shutdown\" command");
    }

    @Override
    public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {
        Main.shutdownNow();
    }
}
