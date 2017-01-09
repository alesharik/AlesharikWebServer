package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.grizzly.http.server.Request;

/**
 * Generate basic error pages
 */
public final class BasicErrorPageConstructor implements ErrorPageConstructor {

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        StringBuilder content = new StringBuilder();
        if(description != null && !description.isEmpty()) {
            content.append(description);
        }
        if(exception != null) {
            if(content.length() > 0) {
                content.append("\n");
            }
            content.append(ExceptionUtils.getMessage(exception));
        }
        if(content.length() <= 0) {
            return generateBasicErrorPage(status, reasonPhrase);
        } else {
            return generateErrorPageWithDescription(status, reasonPhrase, content.toString());
        }
    }

    private String generateBasicErrorPage(int status, String reasonPhrase) {
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
                status + " " + reasonPhrase +
                "</h1>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";
    }

    private String generateErrorPageWithDescription(int status, String reasonPhrase, String content) {
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
                status + " " + reasonPhrase +
                "</h1>" +
                "<pre>" +
                content +
                "</pre>" +
                "<hr/>" +
                "<p>AlesharikWebServer</p>" +
                "</body>" +
                "</html>";
    }

    @Override
    public boolean support(int status) {
        return true;
    }

    @Override
    public String getName() {
        return "Basic error page generator";
    }
}
