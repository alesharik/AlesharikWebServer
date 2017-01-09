package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.NamedLogger;
import com.alesharik.webserver.logger.storingStrategies.WriteOnLogStoringStrategy;
import com.alesharik.webserver.main.FileManager;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;

//TODO add error pages and checks
public class MainHttpHandler extends org.glassfish.grizzly.http.server.HttpHandler {
    private final RequestHandler requestHandler;
    private final FileManager fileManager;
    private final ErrorPageGenerator errorPageGenerator;

    private final boolean logRequests;
    private NamedLogger logger;

    public MainHttpHandler(RequestHandler requestHandler, FileManager fileManager, boolean logRequests, File logFile, ErrorPageGenerator errorPageGenerator) {
        this.errorPageGenerator = errorPageGenerator;
        this.logRequests = logRequests;
        this.requestHandler = requestHandler;
        this.fileManager = fileManager;

        if(this.logRequests) {
            logger = Logger.createNewNamedLogger("ControlHttpHandler", logFile);
            logger.setStoringStrategyFactory(WriteOnLogStoringStrategy::new);
        }
    }

    @Override
    public void service(Request request, Response response) {
        try {
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
            try {
                String uri = request.getDecodedRequestURI();
                if(this.logRequests) {
                    logger.log(request.getRemoteAddr() + ":" + request.getRemotePort() + ": " + uri, "[Request]", "[" + response.getStatus() + "]");
                }
            } catch (CharConversionException e) {
                Logger.log(e);
            }
        } catch (Exception e) {
            try {
                ControlHttpHandler.writeInfernalServerErrorResponse(request, response, e, errorPageGenerator);
            } catch (IOException e1) {
                Logger.log(e1);
            }
        }
        response.finish();
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
            ControlHttpHandler.writeNotFoundResponse(request, response, errorPageGenerator);
        }
    }
}
