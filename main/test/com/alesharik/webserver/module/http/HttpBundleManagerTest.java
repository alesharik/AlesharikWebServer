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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.api.server.wrapper.bundle.ErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.FilterChain;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.RequestRouter;
import com.alesharik.webserver.api.server.wrapper.bundle.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HttpBundleManagerTest {

    @Before
    public void setUp() throws Exception {
        HttpBundleManager.bundles.clear();
    }

    @Test
    public void testAddClassWithNoAnnotation() throws Exception {
        PrintStream serrMock = mock(PrintStream.class);
        System.setErr(serrMock);
        HttpBundleManager.listenBundle(Test1.class);
        verify(serrMock).println("Http bundle " + Test1.class.getCanonicalName() + " must have HttpBundle annotation!");
    }

    @Test
    public void testAddDuplicate() throws Exception {
        PrintStream serrMock = mock(PrintStream.class);
        System.setErr(serrMock);
        HttpBundleManager.listenBundle(Test2.class);
        HttpBundleManager.listenBundle(Test2.class);
        verify(serrMock).println("HttpBundle test already exists!");
    }

    @Test
    public void testAddCorrect() throws Exception {
        HttpBundleManager.listenBundle(Test2.class);
        assertEquals(Test2.class, HttpBundleManager.getBundleClass("test"));
    }

    private static final class Test1 implements HttpHandlerBundle {

        @Override
        public Validator getValidator() {
            return null;
        }

        @Override
        public RequestRouter getRouter() {
            return null;
        }

        @Override
        public FilterChain[] getFilterChains() {
            return new FilterChain[0];
        }

        @Override
        public HttpHandler[] getHttpHandlers() {
            return new HttpHandler[0];
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }
    }

    @HttpBundle("test")
    private static final class Test2 implements HttpHandlerBundle {

        @Override
        public Validator getValidator() {
            return null;
        }

        @Override
        public RequestRouter getRouter() {
            return null;
        }

        @Override
        public FilterChain[] getFilterChains() {
            return new FilterChain[0];
        }

        @Override
        public HttpHandler[] getHttpHandlers() {
            return new HttpHandler[0];
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }
    }
}