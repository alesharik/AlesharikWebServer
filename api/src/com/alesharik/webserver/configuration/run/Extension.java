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

package com.alesharik.webserver.configuration.run;

import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.module.meta.ScriptElementConverter;
import com.alesharik.webserver.configuration.run.message.MessageManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Extensions run configuration
 */
public interface Extension {
    /**
     * Load and parse configuration
     *
     * @param endpoint the configuration
     */
    void load(@Nonnull ConfigurationEndpoint endpoint, @Nonnull ScriptElementConverter converter);

    /**
     * Execute configuration. Will be invoke in extension's own thread
     */
    void start();

    /**
     * Reload the configuration. Will be invoke in extension's own thread
     *
     * @param last    last configuration
     * @param current current configuration
     */
    void reloadConfig(@Nonnull ConfigurationEndpoint last, @Nonnull ConfigurationEndpoint current, @Nonnull ScriptElementConverter converter);

    /**
     * Shutdown the extension. Will be invoke in extension's own thread
     */
    void shutdown();

    /**
     * Shutdown the extension now. Will be invoke in extension's own thread
     */
    void shutdownNow();

    /**
     * Return extension's command executor
     *
     * @return extension's command executor
     */
    @Nonnull
    CommandExecutor getCommandExecutor();

    /**
     * Return extension's file watchers. All their events will be executed in extension's own thread
     *
     * @return extension's file watchers
     */
    @Nonnull
    List<DirectoryWatcher> getFileWatchers();

    /**
     * Return module's message manager. If module doesn't support messages, return <code>null</code>
     *
     * @return message manager or <code>null</code>
     */
    @Nullable
    MessageManager getMessageManager();

    default void listenPoolThread(Executor executor) {

    }

    /**
     * Every extension must have a name. It can be set by this annotation
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @interface Name {
        String value();
    }
}
