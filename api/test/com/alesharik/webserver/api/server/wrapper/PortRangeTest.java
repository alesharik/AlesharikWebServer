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

import org.glassfish.grizzly.utils.Charsets;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class PortRangeTest {
    @Test
    public void correctnessTest() throws Exception {
        PortRange portRange = new PortRange(1, 2);
        assertEquals(1, portRange.getLower());
        assertEquals(2, portRange.getUpper());

        PortRange portRange1 = new PortRange(1);
        assertEquals(1, portRange1.getLower());
        assertEquals(1, portRange1.getUpper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalArguments() throws Exception {
        PortRange portRange = new PortRange(2, 1);
        assertNotNull(portRange);
    }

    @Test
    public void getPortRangeFromXMLValid() throws Exception {
        Element element = getElementFromString("<portRange>" +
                "<from>1</from>" +
                "<to>3</to>" +
                "</portRange>");
        PortRange portRange = PortRange.formXML(element);
        assertEquals(portRange.getLower(), 1);
        assertEquals(portRange.getUpper(), 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPortRangeFromXMLInvalidContainerName() throws Exception {
        Element element = getElementFromString("<asdf>" +
                "<from>1</from>" +
                "<to>3</to>" +
                "</asdf>");
        PortRange portRange = PortRange.formXML(element);
        assertEquals(1, portRange.getLower());
        assertEquals(3, portRange.getUpper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPortRangeFromXMLInvalidFromTag() throws Exception {
        Element element = getElementFromString("<portRange>" +
                "<to>3</to>" +
                "</portRange>");
        PortRange portRange = PortRange.formXML(element);
        assertNull(portRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPortRangeFromXMLInvalidToTag() throws Exception {
        Element element = getElementFromString("<portRange>" +
                "<from>1</from>" +
                "</portRange>");
        PortRange portRange = PortRange.formXML(element);
        assertNull(portRange);
    }

    @Test
    public void getPortsFromXMLWithPortTag() throws Exception {
        Element element = getElementFromString("<e>" +
                "<port>1</port>" +
                "</e>");
        PortRange portRange = PortRange.getPortsFromXML(element);
        assertEquals(1, portRange.getUpper());
        assertEquals(1, portRange.getLower());
    }

    @Test
    public void getPortsFromXMLWithPortRangeTag() throws Exception {
        Element element = getElementFromString("<e>" +
                "<portRange>" +
                "<from>1</from>" +
                "<to>3</to>" +
                "</portRange>" +
                "</e>");
        PortRange portRange = PortRange.getPortsFromXML(element);
        assertEquals(1, portRange.getLower());
        assertEquals(3, portRange.getUpper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPortsFromXMLWithNoTags() throws Exception {
        Element element = getElementFromString("<e>" +
                "<wtf>" +
                "<asd>1</asd>" +
                "<to>3</to>" +
                "</wtf>" +
                "</e>");
        PortRange portRange = PortRange.getPortsFromXML(element);
        assertNull(portRange);
    }

    private static Element getElementFromString(@Nonnull String str) throws Exception {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(str.getBytes(Charsets.UTF8_CHARSET)));
        return document.getDocumentElement();
    }
}