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

package com.alesharik.webserver.configuration.config.ext.define;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Bit32DefineProviderTest {
    private Properties defProps;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        defProps = System.getProperties();
        properties = new Properties();
        System.setProperties(properties);
    }

    @After
    public void tearDown() {
        System.setProperties(defProps);
    }

    @Test
    public void name() {
        assertEquals("BIT_32", new Bit32DefineProvider().getName());
    }

    @Test
    public void defLin() {
        properties.setProperty("os.name", "Linux");

        properties.setProperty("os.arch", "x86_64");
        assertNull(new Bit32DefineProvider().getDefinition(DefineEnvImpl.ENV));

        properties.setProperty("os.arch", "x86");
        assertEquals("true", new Bit32DefineProvider().getDefinition(DefineEnvImpl.ENV));
    }

    @Test
    public void defWin() {
        properties.setProperty("os.name", "Windows");

        assertEquals(System.getenv("ProgramFiles(x86)") != null ? null : "true", new Bit32DefineProvider().getDefinition(DefineEnvImpl.ENV));
    }
}