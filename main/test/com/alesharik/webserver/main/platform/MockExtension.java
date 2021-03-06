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

package com.alesharik.webserver.main.platform;

import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.extension.CommandExecutor;
import com.alesharik.webserver.configuration.extension.DirectoryWatcher;
import com.alesharik.webserver.configuration.extension.Extension;
import com.alesharik.webserver.configuration.extension.message.MessageManager;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Extension.Name("mock")
public class MockExtension implements Extension {
    static final Extension MOCK = mock(Extension.class);
    static final CommandExecutor COMMAND_EXECUTOR_MOCK = mock(CommandExecutor.class);
    static final List<DirectoryWatcher> WATCHERS = new ArrayList<>();
    static final MessageManager MESSAGE_MANAGER_MOCK = mock(MessageManager.class);

    static {
        when(COMMAND_EXECUTOR_MOCK.getPredicate()).thenReturn(commandName -> false);
    }

    @Override
    public void load(@Nonnull ConfigurationEndpoint endpoint, @Nonnull ScriptElementConverter converter) {
        MOCK.load(endpoint, converter);
    }

    @Override
    public void start() {
        MOCK.start();
    }

    @Override
    public void reloadConfig(@Nonnull ConfigurationEndpoint last, @Nonnull ConfigurationEndpoint current, @Nonnull ScriptElementConverter converter) {
        MOCK.reloadConfig(last, current, converter);
    }

    @Override
    public void shutdown() {
        MOCK.shutdown();
    }

    @Override
    public void shutdownNow() {
        MOCK.shutdownNow();
    }

    @Nonnull
    @Override
    public CommandExecutor getCommandExecutor() {
        return COMMAND_EXECUTOR_MOCK;
    }

    @Nonnull
    @Override
    public List<DirectoryWatcher> getFileWatchers() {
        return WATCHERS;
    }

    @Nullable
    @Override
    public MessageManager getMessageManager() {
        return MESSAGE_MANAGER_MOCK;
    }
}
