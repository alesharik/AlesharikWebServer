package com.alesharik.webserver.handlers;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.NamedLogger;
import com.alesharik.webserver.logger.storing.WriteOnLogStoringStrategy;
import com.alesharik.webserver.server.api.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;

//TODO add error pages and checks
public class MainHttpHandler extends HttpHandler {
    private final FileManager fileManager;
    private final ErrorPageGenerator errorPageGenerator;

    private final boolean logRequests;
    private NamedLogger logger;

    public MainHttpHandler() {
        //TODO write
        this.logRequests = true;
        this.fileManager = new FileManager(new File("./www/"), FileManager.FileHoldingMode.HOLD_AND_CHECK);

        this.errorPageGenerator = new ModularErrorPageGenerator(fileManager);

        if(this.logRequests) {
            logger = Logger.createNewNamedLogger("ControlHttpHandler", new File("./logs/request-log.log"));
            logger.setStoringStrategyFactory(WriteOnLogStoringStrategy::new);
        }
    }

    @Override
    public void service(Request request, Response response) {
        try {
            try {
//                if(requestHandler.canHandleRequest(request)) {
//                    requestHandler.handleRequest(request, response);
//                } else {
                    handleRequest(request, response);
//                }
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

    @Override
    public String getHandlerName() {
        return "control-http-handler";
    }

    @Override
    public String getName() {
        return "main-http-handler";
    }

    @Override
    public void parse(@Nullable Element configNode) {

    }

    @Override
    public void reload(@Nullable Element configNode) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdownNow() {

    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
