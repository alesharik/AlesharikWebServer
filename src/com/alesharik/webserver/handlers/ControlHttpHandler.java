package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.RequestHandlerList;
import com.alesharik.webserver.control.ControlRequestHandler;
import com.alesharik.webserver.control.dataHolding.AdminDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.Helpers;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.UUID;

/**
 * This class used for handle http requests in control mode
 */
public final class ControlHttpHandler extends org.glassfish.grizzly.http.server.HttpHandler {
    private ControlRequestHandler requestHandler;
    private FileManager fileManager;
    private RequestHandlerList requestHandlerList = new RequestHandlerList();

    public ControlHttpHandler(FileManager fileManager, AdminDataHolder adminDataHolder) {
        requestHandler = new ControlRequestHandler(fileManager, adminDataHolder);
        this.fileManager = fileManager;
    }

    public ControlRequestHandler getControlRequestHandler() {
        return requestHandler;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        if(requestHandler.canHandleRequest(request)) {
            requestHandler.handleRequest(request, response);
        } else if(requestHandlerList.canHandleRequest(request)) {
            requestHandlerList.handleRequest(request, response);
        } else {
            handleRequest(request, response);
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
        Logger.log(uri);
        String file = uri.equals("/") ? "/index.html" : uri;
        if(fileManager.exists(file, true)) {
            if(!file.equals("/index.html") && !file.equals("/lib/font-awesome/font-awesome.min.css") && !file.equals("/styles/bootstrap.css")
                    && !file.equals("/styles/main.css") && !file.equals("/lib/jquery/jquery.min.js") && !file.equals("/lib/bootstrap/bootstrap.min.js")
                    && !file.equals("/lib/jquery/jquery.min.map") && !file.equals("/lib/bootstrap/fonts.css") && !file.equals("/lib/bootstrap/fonts/CWB0XYA8bzo0kSThX0UTuA.woff2")) {
                Cookie uuid = Helpers.getCookieForName("UUID", request.getCookies());
                UUID uuid1;
                try {
                    uuid1 = UUID.fromString(uuid.getValue());
                } catch (IllegalArgumentException | NullPointerException e) {
                    uuid1 = null;
                }
                if(uuid == null || !requestHandler.isSessionValid(uuid1)) {
                    if(!response.isCommitted()) {
                        response.reset();
                        response.setStatus(HttpStatus.FOUND_302);
                        response.setHeader(Header.Location, "/index.html");
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
            if(!response.isCommitted()) {
                response.reset();
                HtmlHelper.setErrorAndSendErrorPage(
                        request, response,
                        response.getErrorPageGenerator(),
                        404, HttpStatus.NOT_FOUND_404.getReasonPhrase(),
                        null,
                        null);
            }
        }
    }
}
