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
