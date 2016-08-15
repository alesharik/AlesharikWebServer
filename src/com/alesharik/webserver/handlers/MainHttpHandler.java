package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.FileManager;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;

//TODO add error pages and checks
public class MainHttpHandler extends org.glassfish.grizzly.http.server.HttpHandler {
    private RequestHandler requestHandler;
    private FileManager fileManager;

    public MainHttpHandler(RequestHandler requestHandler, FileManager fileManager) {
        this.requestHandler = requestHandler;
        this.fileManager = fileManager;
    }

    @Override
    public void service(Request request, Response response) {
        try {
            if(requestHandler.canHandleRequest(request)) {
                requestHandler.handleRequest(request, response);
            } else {
                handleRequest(request, response);
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            Logger.log(e);
        }
    }

    @SuppressWarnings("Duplicates")
    private void handleRequest(Request request, Response response) throws IOException {
        String uri = request.getDecodedRequestURI();
        Logger.log(uri);
        String file = uri.equals("/") ? "/index.html" : uri;
        if(fileManager.exists(file)) {
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
