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

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ModuleHolderTest {
    private ConfigurationImpl.ModuleHolder moduleHolder;

    @Before
    public void setUp() throws Exception {
        Module module = mock(Module.class);
        when(module.getName()).thenReturn("test");
        moduleHolder = new ConfigurationImpl.ModuleHolder(module, "test");
    }

    @Test
    public void startShutdownTest() throws Exception {
        assertFalse(moduleHolder.isRunning());

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.shutdown();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdown();

        moduleHolder.shutdown();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdown();
    }

    @Test
    public void startShutdownNowTest() throws Exception {
        assertFalse(moduleHolder.isRunning());

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.start();
        assertTrue(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).start();

        moduleHolder.shutdownNow();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdownNow();

        moduleHolder.shutdownNow();
        assertFalse(moduleHolder.isRunning());
        verify(moduleHolder.getModule(), times(1)).shutdownNow();
    }

    @Test
    public void checkUncheck() throws Exception {
        assertFalse(moduleHolder.isChecked());

        moduleHolder.check();
        assertTrue(moduleHolder.isChecked());

        moduleHolder.uncheck();
        assertFalse(moduleHolder.isChecked());
    }

    @Test
    public void getType() throws Exception {
        assertEquals(moduleHolder.getType(), "test");
    }

    @Test
    public void mainCheckUncheck() throws Exception {
        assertFalse(moduleHolder.mainIsChecked());

        moduleHolder.mainCheck();
        assertTrue(moduleHolder.mainIsChecked());

        moduleHolder.mainUncheck();
        assertFalse(moduleHolder.mainIsChecked());
    }

    @Test
    public void parseReload() throws Exception {
        Element element = mock(Element.class);

        moduleHolder.parse(element);
        verify(moduleHolder.getModule(), times(1)).parse(element);

        moduleHolder.reload();
        verify(moduleHolder.getModule(), times(1)).reload(element);

        element = mock(Element.class);
        moduleHolder.reload(element);
        moduleHolder.reload();
        verify(moduleHolder.getModule(), times(2)).reload(element);
    }
}