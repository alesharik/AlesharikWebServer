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

package com.alesharik.webserver.module.http.bundle.impl.error.impl;

import com.alesharik.webserver.exception.ExceptionWithoutStacktrace;
import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.module.http.bundle.impl.error.ErrorPageProvider;
import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import org.junit.Test;

import static com.alesharik.webserver.test.http.HttpMockUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultErrorPageProviderTest {
    private final ErrorPageProvider provider = new DefaultErrorPageProvider();

    @Test
    public void priority() {
        assertEquals(Integer.MIN_VALUE, provider.getPriority());
    }

    @Test
    public void error() throws Exception {
        Request request = request().build();
        Response response = response();
        response.respond(HttpStatus.INTERNAL_SERVER_ERROR_500);

        String expect = "<html>" +
                "<head>" +
                "<style>" +
                "h1, p {" +
                "   text-align: center;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" +
                500 + " " + "Internal Server Error" +
                "</h1>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";

        assertTrue(provider.isApplicable(request, response));
        provider.sendErrorPage(request, response);

        verify(response)
                .respond(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(expect.getBytes(response.getBodyCharset()));
    }

    @Test
    public void errorWithBody() throws Exception {
        Request request = request().build();
        Response response = response();
        response.respond(HttpStatus.INTERNAL_SERVER_ERROR_500);
        response.getWriter().write("asd");

        String expect = "<html>" +
                "<head>" +
                "<style>" +
                "h1, p, pre {" +
                "   text-align: center;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" +
                500 + " " + "internal Server Error" +
                "</h1>" +
                "<pre>" +
                "asd" +
                "</pre>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";


        assertTrue(provider.isApplicable(request, response));
        provider.sendErrorPage(request, response);

        verify(response)
                .respond(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(expect.getBytes(response.getBodyCharset()));
    }

    @Test
    public void exceptionWithDebug() {
        Debug.enable();

        assertTrue(provider.canSendExceptionErrorPages());
        Request request = request().build();
        Response response = response();
        response.getWriter().write("asd");

        ExceptionWithoutStacktrace e = new ExceptionWithoutStacktrace("a");
        String expect = "<html>" +
                "<head>" +
                "<style>" +
                "h1, p, pre {" +
                "   text-align: center;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" +
                500 + " " + "internal Server Error" +
                "</h1>" +
                "<pre>" +
                e.getMessage() +
                "</pre>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";

        provider.sendExceptionErrorPage(request, response, e);

        verify(response)
                .respond(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(expect.getBytes(response.getBodyCharset()));

        Debug.disable();
    }

    @Test
    public void exceptionWithoutDebug() {
        Debug.disable();

        assertTrue(provider.canSendExceptionErrorPages());
        Request request = request().build();
        Response response = response();
        response.getWriter().write("asd");

        ExceptionWithoutStacktrace e = new ExceptionWithoutStacktrace("a");

        provider.sendExceptionErrorPage(request, response, e);

        verify(response)
                .respond(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(new byte[0]);
    }
}