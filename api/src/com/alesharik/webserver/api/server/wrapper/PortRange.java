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

import lombok.Getter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

@Immutable
public class PortRange implements Serializable {
    private static final long serialVersionUID = 3060816768811688047L;

    @Getter
    protected final int lower;
    @Getter
    protected final int upper;

    /**
     * @param lower from value
     * @param upper to value
     * @throws IllegalArgumentException if lower value > upper value
     */
    public PortRange(int lower, int upper) {
        if(lower > upper)
            throw new IllegalArgumentException("Lower value must be lower than upper value!");
        this.lower = lower;
        this.upper = upper;
    }

    public PortRange(int port) {
        this(port, port);
    }

    /**
     * Container element must be named <code>portRange</code>. It must contains <code>from</code> and <code>to</code> tags.<br>
     * <pre>
     * {@code <portRange>
     *     <from>1</from>
     *     <to>12</to>
     * </portRange>}
     * </pre>
     *
     * @param element the element
     * @return PortRange
     * @throws IllegalArgumentException if element has invalid tags or doesn't have required tags
     */
    @Nonnull
    public static PortRange formXML(@Nonnull Element element) {
        if(!element.getTagName().equals("portRange"))
            throw new IllegalArgumentException("Input tag must be 'portRange'!");
        Node fromNode = element.getElementsByTagName("from").item(0);
        if(fromNode == null)
            throw new IllegalArgumentException("PortRange must contains 'from' tag!");
        Node toNode = element.getElementsByTagName("to").item(0);
        if(toNode == null)
            throw new IllegalArgumentException("PortRange must contains 'to' tag!");

        return new PortRange(Integer.parseInt(fromNode.getTextContent()), Integer.parseInt(toNode.getTextContent()));
    }

    /**
     * Tries to find in xml <code>port</code> or <code>portRange</code> nodes
     *
     * @param element the element to perform search
     * @return found port or port range
     * @throws IllegalArgumentException if element has invalid tags or doesn't have required tags
     */
    @Nonnull
    public static PortRange getPortsFromXML(@Nonnull Element element) {
        Node portNode = element.getElementsByTagName("port").item(0);
        if(portNode != null)
            return new PortRange(Integer.parseInt(portNode.getTextContent()));
        else {
            Element portRangeElement = (Element) element.getElementsByTagName("portRange").item(0);
            if(portRangeElement != null)
                return formXML(portRangeElement);
            else
                throw new IllegalArgumentException("Element doesn't have any 'port' or 'portRange' tags!");
        }
    }
}
