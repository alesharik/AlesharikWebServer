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

package com.alesharik.webserver.api.server.wrapper;

import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import lombok.Getter;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.io.File;

@Getter
public final class SSLConfiguration {
    private final KeyStoreConfiguration configuration;
    private final String protocol;

    public SSLConfiguration(KeyStoreConfiguration configuration, String protocol) {
        this.configuration = configuration;
        this.protocol = protocol;
    }

    public static SSLConfiguration getConfiguration(@Nonnull Element element) {
        KeyStoreConfiguration keyStoreConfiguration = KeyStoreConfiguration.getConfiguration(element);
        String protocol = XmlHelper.getString("protocol", element, false);
        return new SSLConfiguration(keyStoreConfiguration, protocol);
    }

    @Getter
    public final static class KeyStoreConfiguration {
        private final String type;
        private final File file;
        private final String password;

        public KeyStoreConfiguration(String type, File file, String password) {
            this.type = type;
            this.file = file;
            this.password = password;
        }

        public static KeyStoreConfiguration getConfiguration(@Nonnull Element element) {
            String type = XmlHelper.getString("type", element, false);
            File file = XmlHelper.getFile("file", element, true);
            if(!file.exists() || file.isDirectory())
                throw new ConfigurationParseError("File must be real file!");

            String password = XmlHelper.getString("password", element, true);
            return new KeyStoreConfiguration(type, file, password);
        }
    }
}
