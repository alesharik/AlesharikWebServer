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

import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.module.http.bundle.impl.error.ErrorPageProvider;
import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.http.data.MimeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class DefaultErrorPageProvider implements ErrorPageProvider {
    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isApplicable(Request request, Response response) {
        return true;
    }

    @Override
    public void sendErrorPage(Request request, Response response) {
        if(response.getBody().length > 0) {
            byte[] body = response.getBody();
            response.clearBody();
            response.getWriter().write(getContentPage(response.getStatus(), new String(body, response.getBodyCharset())));
        } else
            response.getWriter().write(getBasicPage(response.getStatus()));
    }

    @Override
    public boolean canSendExceptionErrorPages() {
        return true;
    }

    @Override
    public void sendExceptionErrorPage(Request request, Response response, Exception e) {
        response.recycle();
        response.respond(HttpStatus.INTERNAL_SERVER_ERROR_500);
        if(Debug.isEnabled()) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                e.printStackTrace(printWriter);
                String content = new String(out.toByteArray(), StandardCharsets.UTF_8);
                response.setType(new MimeType("text", "html"), StandardCharsets.UTF_8);
                response.getWriter().write(getContentPage(response.getStatus(), content));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static String getBasicPage(HttpStatus status) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "h1, p {" +
                "   text-align: center;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" +
                status.getCode() + " " + status.getStatus() +
                "</h1>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";
    }


    private static String getContentPage(HttpStatus status, String content) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "h1, p, pre {" +
                "   text-align: center;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<h1>" +
                status.getCode() + " " + status.getStatus() +
                "</h1>" +
                "<pre>" +
                content +
                "</pre>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";
    }
}
