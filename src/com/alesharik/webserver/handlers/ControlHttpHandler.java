package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.RequestHandlerList;
import com.alesharik.webserver.control.ControlRequestHandler;
import com.alesharik.webserver.control.dataHolding.AdminDataHolder;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.FileManager;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;

/**
 * This class used for handle http requests in control mode
 */
public class ControlHttpHandler extends org.glassfish.grizzly.http.server.HttpHandler {
    private RequestHandler requestHandler;
    private FileManager fileManager;
    private RequestHandlerList requestHandlerList = new RequestHandlerList();

    public ControlHttpHandler(FileManager fileManager, AdminDataHolder adminDataHolder) {
        requestHandler = new ControlRequestHandler(fileManager, fileManager.getRootFolder(), adminDataHolder);
        this.fileManager = fileManager;
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

    @SuppressWarnings("Duplicates")
    private void handleRequest(Request request, Response response) throws IOException {
        String uri = request.getDecodedRequestURI();
        Logger.log(uri);
        String file = uri.equals("/") ? "/index.html" : uri;
        if(fileManager.exists(file, true)) {
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
