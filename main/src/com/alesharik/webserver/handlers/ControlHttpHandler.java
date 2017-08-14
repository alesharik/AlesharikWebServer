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

package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.GrizzlyUtils;
import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.control.ControlRequestHandler;
import com.alesharik.webserver.control.data.storage.AdminDataStorageImpl;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.NamedLogger;
import com.alesharik.webserver.logger.storing.WriteOnLogStoringStrategy;
import com.alesharik.webserver.server.api.RequestHandler;
import com.alesharik.webserver.server.api.RequestHandlerList;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.utils.Charsets;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * This class used for handle http requests in control mode
 */
@Deprecated
public final class ControlHttpHandler extends HttpHandler {
    private final ControlRequestHandler requestHandler;
    private final FileManager fileManager;
    private final RequestHandlerList requestHandlerList;
    private final ErrorPageGenerator errorPageGenerator;

    private final boolean logRequests;
    private NamedLogger logger;

    /**
     * @param logFile can be null
     */
    public ControlHttpHandler(FileManager fileManager, AdminDataStorageImpl adminDataStorageImpl, boolean logRequests, File logFile, ErrorPageGenerator errorPageGenerator) {
        this.errorPageGenerator = errorPageGenerator;
        this.logRequests = logRequests;
        this.requestHandler = new ControlRequestHandler(fileManager, adminDataStorageImpl);
        this.fileManager = fileManager;
        this.requestHandlerList = new RequestHandlerList();

        if(this.logRequests) {
            logger = Logger.createNewNamedLogger("ControlHttpHandler", logFile);
            logger.setStoringStrategyFactory(WriteOnLogStoringStrategy::new);
        }
    }

    public ControlRequestHandler getControlRequestHandler() {
        return requestHandler;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        try {
            if(requestHandler.canHandleRequest(request)) {
                requestHandler.handleRequest(request, response);
            } else if(requestHandlerList.canHandleRequest(request)) {
                requestHandlerList.handleRequest(request, response);
                String uri = request.getDecodedRequestURI();
                if(this.logRequests) {
                    logger.log(request.getRemoteAddr() + ":" + request.getRemotePort() + ": " + uri, "[Request]", "[" + response.getStatus() + "]");
                }
            } else {
                handleRequest(request, response);
                String uri = request.getDecodedRequestURI();
                if(this.logRequests) {
                    logger.log(request.getRemoteAddr() + ":" + request.getRemotePort() + ": " + uri, "[Request]", "[" + response.getStatus() + "]");
                }
            }
            response.finish();
        } catch (Exception e) {
            writeInfernalServerErrorResponse(request, response, e, errorPageGenerator);
        }
    }

    public void addRequestHandler(RequestHandler handler) {
        requestHandlerList.remove(handler);
    }

    public void removeRequestHandler(RequestHandler handler) {
        requestHandlerList.remove(handler);
    }

    private void handleRequest(Request request, Response response) throws IOException {
        String uri = request.getDecodedRequestURI();
        String file = uri.equals("/") ? "/index.html" : uri;
        if(fileManager.exists(file, true)) {
            if(!file.equals("/index.html") && !file.equals("/lib/font-awesome/font-awesome.min.css") && !file.equals("/styles/bootstrap.css")
                    && !file.equals("/styles/main.css") && !file.equals("/lib/jquery/jquery.min.js") && !file.equals("/lib/bootstrap/bootstrap.min.js")
                    && !file.equals("/lib/jquery/jquery.min.map") && !file.equals("/lib/bootstrap/fonts.css") && !file.equals("/lib/bootstrap/fonts/CWB0XYA8bzo0kSThX0UTuA.woff2")) {
                Cookie uuid = GrizzlyUtils.getCookieForName("UUID", request.getCookies());
                UUID uuid1;
                try {
                    uuid1 = UUID.fromString(uuid.getValue());
                } catch (IllegalArgumentException | NullPointerException e) {
                    uuid1 = null;
                }
                if(uuid == null || !requestHandler.isSessionValid(uuid1)) {
                    if(!response.isCommitted()) {
                        response.reset();
                        response.sendRedirect("/index.html");
                    }
                    return;
                }
            }

            String type = MIMETypes.findType(file.substring(file.contains(".") ? file.lastIndexOf('.') : 0));
            byte[] bytes = fileManager.readFile(file);
            response.setContentType((type == null) ? "text/plain" : type);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        } else {
            writeNotFoundResponse(request, response, errorPageGenerator);
        }
    }

    static void writeNotFoundResponse(Request request, Response response, ErrorPageGenerator errorPageGenerator) throws IOException {
        if(!response.isCommitted()) {
            response.reset();
            response.setContentType("text/html");
            String responseText = errorPageGenerator.generate(request, 404, new String(HttpStatus.NOT_FOUND_404.getReasonPhraseBytes(), Charsets.ASCII_CHARSET), null, null);
            response.setContentLength(responseText.length());
            response.getWriter().append(responseText);
        }
    }

    static void writeInfernalServerErrorResponse(Request request, Response response, Exception e, ErrorPageGenerator errorPageGenerator) throws IOException {
        if(!response.isCommitted()) {
            response.reset();

            response.setContentType("text/html");
            String responseText = errorPageGenerator.generate(request, 500, new String(HttpStatus.INTERNAL_SERVER_ERROR_500.getReasonPhraseBytes(), Charsets.ASCII_CHARSET), null, e);
            response.setContentLength(responseText.length());
            response.getWriter().append(responseText);
        }
    }
}
