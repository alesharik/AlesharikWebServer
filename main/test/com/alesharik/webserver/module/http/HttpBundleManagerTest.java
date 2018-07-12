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

import com.alesharik.webserver.module.http.bundle.ErrorHandler;
import com.alesharik.webserver.module.http.bundle.HttpBundle;
import com.alesharik.webserver.module.http.bundle.HttpHandlerBundle;
import com.alesharik.webserver.module.http.bundle.Validator;
import com.alesharik.webserver.module.http.bundle.processor.HttpProcessor;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
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
        HttpBundleManager.bundles.clear();

        PrintStream serrMock = mock(PrintStream.class);
        System.setErr(serrMock);
        HttpBundleManager.listenBundle(Test2.class);
        HttpBundleManager.listenBundle(Test2.class);

        assertEquals(1, HttpBundleManager.bundles.size());
    }

    @Test
    public void testAddCorrect() throws Exception {
        HttpBundleManager.listenBundle(Test2.class);
        assertEquals(Test2.class, HttpBundleManager.getBundleClass("test"));
    }

    private static final class Test1 implements HttpHandlerBundle {
        @Nonnull
        @Override
        public Validator getValidator() {
            return null;
        }

        @Nonnull
        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Nonnull
        @Override
        public HttpProcessor getProcessor() {
            return null;
        }
    }

    @HttpBundle("test")
    private static final class Test2 implements HttpHandlerBundle {
        @Nonnull
        @Override
        public Validator getValidator() {
            return null;
        }

        @Nonnull
        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Nonnull
        @Override
        public HttpProcessor getProcessor() {
            return null;
        }
    }
}