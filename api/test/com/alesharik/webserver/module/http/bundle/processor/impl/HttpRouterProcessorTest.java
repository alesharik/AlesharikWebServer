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

package com.alesharik.webserver.module.http.bundle.processor.impl;

import com.alesharik.webserver.api.server.wrapper.bundle.processor.Handler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.alesharik.webserver.module.http.bundle.processor.impl.HttpRouterProcessor.router;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpRouterProcessorTest {
    private static void mockHandler(Handler handler) {
        doAnswer(invocation -> {
            try {
                return invocation.callRealMethod();
            } catch (ReThrowException e) {
                throw e.getCause();
            }
        }).when(handler).process(any(), any());
    }

    @Test
    public void goToPath() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        processor.process(request, Response.getResponse());

        ArgumentCaptor<Request> argumentCaptor = ArgumentCaptor.forClass(Request.class);
        verify(handler, times(1)).handle(argumentCaptor.capture(), any());
        assertSame(request, argumentCaptor.getValue());
        verify(def, never()).handle(any(), any());
    }

    @Test
    public void goToDefaultPath() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/nope");

        processor.process(request, Response.getResponse());

        ArgumentCaptor<Request> argumentCaptor = ArgumentCaptor.forClass(Request.class);
        verify(def, times(1)).handle(argumentCaptor.capture(), any());
        assertSame(request, argumentCaptor.getValue());
        verify(handler, never()).handle(any(), any());
    }

    @Test
    public void errorInHandlerWithErrorHandler() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        doThrow(new TestException()).when(handler).handle(any(), any());
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);
        processor.onError(httpErrorHandler);
        processor.process(request, Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void errorInDefaultHandlerWithErrorHandler() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        doThrow(new TestException()).when(def).handle(any(), any());
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/asdd");

        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);
        processor.onError(httpErrorHandler);
        processor.process(request, Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void errorInHandlerWithErrorHandlerWithReThrow() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        doThrow(new ReThrowException(new TestException())).when(handler).handle(any(), any());
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);
        processor.onError(httpErrorHandler);
        processor.process(request, Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void errorInDefaultHandlerWithErrorHandlerWithReThrow() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        doThrow(new ReThrowException(new TestException())).when(def).handle(any(), any());
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/asdd");

        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);
        processor.onError(httpErrorHandler);
        processor.process(request, Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void errorInHandlerWithNoErrorHandler() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        doThrow(new TestException()).when(handler).handle(any(), any());
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        try {
            processor.process(request, Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    @Test
    public void errorInDefaultHandlerNoWithErrorHandler() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        doThrow(new TestException()).when(def).handle(any(), any());
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/asdd");

        try {
            processor.process(request, Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    @Test
    public void errorInHandlerWithNoErrorHandlerAndWithReThrow() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        doThrow(new ReThrowException(new TestException())).when(handler).handle(any(), any());
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        try {
            processor.process(request, Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    @Test
    public void errorInDefaultHandlerNoWithErrorHandlerAndWithReThrow() throws Exception {
        HttpRouterProcessor processor = router();
        Handler handler = mock(Handler.class);
        Handler def = mock(Handler.class);
        mockHandler(handler);
        mockHandler(def);
        doThrow(new ReThrowException(new TestException())).when(def).handle(any(), any());
        processor.path("/test", handler);
        processor.defaultPath(def);

        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/asdd");

        try {
            processor.process(request, Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    @Test
    public void goToNull() {
        HttpRouterProcessor processor = router();
        Request request = mock(Request.class);
        when(request.getContextPath()).thenReturn("/test");

        processor.process(request, mock(Response.class));
    }

    private static final class TestException extends RuntimeException {

        private static final long serialVersionUID = -1851927193280186384L;
    }
}