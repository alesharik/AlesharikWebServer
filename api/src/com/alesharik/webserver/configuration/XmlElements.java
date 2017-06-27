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

package com.alesharik.webserver.configuration;

import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;

/**
 * This class parse some useful classes form xml
 */
@UtilityClass
public class XmlElements {
    private static final String DEFAULT_HOST = "0.0.0.0";

    public static NetworkListener getNetworkListenerFromXML(Element element) {
//        NetworkListener networkListener = new NetworkListener(getString(element, "name"),
//                getString(element, "host", DEFAULT_HOST),
//                PortRange.getPortsFromXML(element));
//        networkListener.registerAddOn(AddOn)
        return null;
    }

    @Nonnull
    static String getString(@Nonnull Element element, @Nonnull String nodeName) {
        Node node = element.getElementsByTagName(nodeName).item(0);
        if(node == null)
            throw new IllegalArgumentException("Element must contains " + nodeName + " node!");
        else
            return node.getTextContent();
    }

    @Nonnull
    static String getString(@Nonnull Element element, @Nonnull String nodeName, @Nonnull String def) {
        Node node = element.getElementsByTagName(nodeName).item(0);
        if(node == null)
            return def;
        else
            return node.getTextContent();
    }
}
