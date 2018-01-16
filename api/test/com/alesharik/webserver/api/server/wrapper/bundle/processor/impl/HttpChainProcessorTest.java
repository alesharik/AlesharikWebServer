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

package com.alesharik.webserver.api.server.wrapper.bundle.processor.impl;

import com.alesharik.webserver.api.server.wrapper.bundle.processor.Filter;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.Handler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.alesharik.webserver.api.server.wrapper.bundle.processor.impl.HttpChainProcessor.chain;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HttpChainProcessorTest {
    @Test
    public void works() throws Exception {
        Filter filter = mock(Filter.class);
        when(filter.filter(any(), any())).thenReturn(true);
        Handler handler = mock(Handler.class);
        HttpProcessor processor = mock(HttpProcessor.class);

        HttpChainProcessor chain = chain();

        chain.filter(filter).then(handler).process(processor).then(handler);

        Response response = Response.getResponse();
        chain.process(mock(Request.class), response);

        ArgumentCaptor<Response> responseArgumentCaptor = ArgumentCaptor.forClass(Response.class);
        verify(filter, times(1)).filter(any(), responseArgumentCaptor.capture());
        assertSame(response, responseArgumentCaptor.getValue());
        ArgumentCaptor<Response> responseArgumentCaptor1 = ArgumentCaptor.forClass(Response.class);
        verify(processor, times(1)).process(any(), responseArgumentCaptor1.capture());
        assertSame(response, responseArgumentCaptor1.getValue());
        ArgumentCaptor<Response> responseArgumentCaptor2 = ArgumentCaptor.forClass(Response.class);
        verify(handler, times(2)).handle(any(), responseArgumentCaptor2.capture());
        assertSame(response, responseArgumentCaptor2.getValue());
    }

    @Test
    public void testWithFilterDeny() throws Exception {
        Filter filter = mock(Filter.class);
        when(filter.filter(any(), any())).thenReturn(false);
        Handler handler1 = mock(Handler.class);
        Handler handler2 = mock(Handler.class);

        chain()
                .then(handler1)
                .filter(filter)
                .then(handler2)
                .process(mock(Request.class), Response.getResponse());
        verify(handler1, times(1)).handle(any(), any());
        verify(handler2, never()).handle(any(), any());
        verify(filter, times(1)).filter(any(), any());
    }

    @Test
    public void testExceptionWithErrorHandler() {
        Handler handler = (request, response) -> {
            throw new TestException();
        };
        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);

        chain()
                .then(handler)
                .onError(httpErrorHandler)
                .process(mock(Request.class), Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void testReThrowExceptionWithErrorHandler() {
        Handler handler = (request, response) -> {
            throw new ReThrowException(new TestException());
        };
        HttpErrorHandler httpErrorHandler = mock(HttpErrorHandler.class);

        chain()
                .then(handler)
                .onError(httpErrorHandler)
                .process(mock(Request.class), Response.getResponse());

        verify(httpErrorHandler, times(1)).handleException(any(TestException.class), any(), any());
    }

    @Test
    public void testExceptionWithoutErrorHandler() {
        Handler handler = (request, response) -> {
            throw new TestException();
        };

        try {
            chain()
                    .then(handler)
                    .process(mock(Request.class), Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    @Test
    public void testReThrowExceptionWithoutErrorHandler() {
        Handler handler = (request, response) -> {
            throw new ReThrowException(new TestException());
        };

        try {
            chain()
                    .then(handler)
                    .process(mock(Request.class), Response.getResponse());
        } catch (ReThrowException e) {
            assertTrue(e.getCause() instanceof TestException);
            return;
        }
        fail();
    }

    static final class TestException extends RuntimeException {

        private static final long serialVersionUID = 5048605951695328978L;
    }
}