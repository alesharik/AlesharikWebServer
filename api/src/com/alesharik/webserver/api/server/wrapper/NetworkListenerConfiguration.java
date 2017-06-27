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
import lombok.Getter;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class NetworkListenerConfiguration {
    private final String name;

    private final String host;
    private final PortRange portRange;
    private final List<String> addonNames;

    private final String compression;
    private final List<String> compressableMimeTypes;

    private final SSLConfiguration sslConfiguration;

    public NetworkListenerConfiguration(String name, String host, PortRange portRange, List<String> addonNames, String compression, List<String> compressableMimeTypes, SSLConfiguration sslConfiguration) {
        this.name = name;
        this.host = host;
        this.portRange = portRange;
        this.addonNames = addonNames;
        this.compression = compression;
        this.compressableMimeTypes = compressableMimeTypes;
        this.sslConfiguration = sslConfiguration;
    }

    public static NetworkListenerConfiguration getConfiguration(@Nonnull Element element) {
        String name = XmlHelper.getString("name", element, true);
        String host = XmlHelper.getString("host", element, true);
        PortRange portRange = PortRange.formXML(element);
        List<String> addons = XmlHelper.getList("addons", "addon", element, false);
        if(addons == null)
            addons = new ArrayList<>();
        String compression = XmlHelper.getString("compression", element, false);
        List<String> compressableMimeTypes = XmlHelper.getList("compressableMimeTypes", "mimeType", element, false);
        if(compressableMimeTypes == null)
            compressableMimeTypes = new ArrayList<>();
        Element sslConfigurationElement = XmlHelper.getXmlElement("ssl", element, false);
        SSLConfiguration configuration = null;
        if(sslConfigurationElement != null) {
            configuration = SSLConfiguration.getConfiguration(sslConfigurationElement);
        }
        return new NetworkListenerConfiguration(name, host, portRange, addons, compression, compressableMimeTypes, configuration);
    }
}
