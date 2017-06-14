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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;

/**
 * Console commands called by user with server console. Implementing class must have empty constructor!
 */
public interface ConsoleCommand {
    /**
     * Return command unique name
     */
    @Nonnull
    String getName();

    /**
     * Return command description, which is shown in <code>help list</code>
     */
    @Nonnull
    String getDescription();

    void printHelp(PrintStream printStream);

    /**
     * Handle command
     *
     * @param command line with command
     * @param out     output stream
     * @param reader  {@link Reader} for reading additional data
     */
    void handle(@Nonnull String command, @Nonnull PrintStream out, @Nonnull Reader reader);

    interface Reader {
        /**
         * Read new line. Return user input line without <code>\n</code>. <code>null</code> means that we reached end of stream
         */
        @Nullable
        String readLine();

        /**
         * Return true if console can properly read passwords from user
         */
        boolean passwordSupported();

        /**
         * Read password from user
         *
         * @return null if {@link #passwordSupported()} is false
         */
        @Nullable
        char[] readPassword();
    }
}
