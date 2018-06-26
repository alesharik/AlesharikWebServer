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

package com.alesharik.webserver.main.console;

import com.alesharik.webserver.api.agent.Ignored;
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

    @Ignored
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

    @Ignored
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

    @Ignored
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

    @Ignored
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