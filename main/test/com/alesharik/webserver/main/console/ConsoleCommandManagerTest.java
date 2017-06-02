package com.alesharik.webserver.main.console;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class ConsoleCommandManagerTest {
    @Test
    public void testManager() throws Exception {
        ConsoleCommandManager.listenCommand(TestAConsoleCommand.class);
        ConsoleCommandManager.listenCommand(TestBConsoleCommand.class);
        ConsoleCommandManager.listenCommand(TestInvalidConstructorConsoleCommand.class);
        ConsoleCommandManager.listenCommand(ThrowingConsoleCommand.class);

        assertEquals(ConsoleCommandManager.getCommands().size(), 2);

        assertEquals(ConsoleCommandManager.getCommand("testA").getName(), "testA");
        assertEquals(ConsoleCommandManager.getCommand("testB").getName(), "testB");
        assertNull(ConsoleCommandManager.getCommand("testC"));
        assertNull(ConsoleCommandManager.getCommand("testD"));

        assertTrue(ConsoleCommandManager.containsCommand("testA"));
        assertTrue(ConsoleCommandManager.containsCommand("testB"));
        assertFalse(ConsoleCommandManager.containsCommand("testC"));
        assertFalse(ConsoleCommandManager.containsCommand("testD"));
    }

    private static final class TestAConsoleCommand implements ConsoleCommand {

        @Nonnull
        @Override
        public String getName() {
            return "testA";
        }

        @Nonnull
        @Override
        public String getDescription() {
            return "desc";
        }

        @Override
        public void printHelp(PrintStream printStream) {

        }

        @Override
        public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {

        }
    }

    private static final class TestBConsoleCommand implements ConsoleCommand {

        @Nonnull
        @Override
        public String getName() {
            return "testB";
        }

        @Nonnull
        @Override
        public String getDescription() {
            return "desc";
        }

        @Override
        public void printHelp(PrintStream printStream) {

        }

        @Override
        public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {

        }
    }

    private static final class TestInvalidConstructorConsoleCommand implements ConsoleCommand {
        private final String asd;

        private TestInvalidConstructorConsoleCommand(String asd) {
            this.asd = asd;
        }

        @Nonnull
        @Override
        public String getName() {
            return "testC";
        }

        @Nonnull
        @Override
        public String getDescription() {
            return "desc";
        }

        @Override
        public void printHelp(PrintStream printStream) {

        }

        @Override
        public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {
            out.print(asd);
        }
    }

    private static final class ThrowingConsoleCommand implements ConsoleCommand {
        public ThrowingConsoleCommand() {
            throw new RuntimeException("It works!");
        }

        @Nonnull
        @Override
        public String getName() {
            return "testD";
        }

        @Nonnull
        @Override
        public String getDescription() {
            return "desc";
        }

        @Override
        public void printHelp(PrintStream printStream) {

        }

        @Override
        public void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader) {

        }
    }
}