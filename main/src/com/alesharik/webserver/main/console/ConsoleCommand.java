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
