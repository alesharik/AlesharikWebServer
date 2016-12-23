package com.alesharik.webserver.generators;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.grizzly.http.server.Request;

/**
 * Generate basic error pages
 */
public final class BasicErrorPageGenerator {
    //    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        String content = "";
        if(description != null && !description.isEmpty()) {
            content += description;
        }
        if(exception != null) {
            if(!content.isEmpty()) {
                content += "\n";
            }
            content += ExceptionUtils.getMessage(exception);
        }
        if(content.isEmpty()) {
            return generateBasicErrorPage(status, reasonPhrase);
        } else {
            return generateDescriptedErrorpage(status, reasonPhrase, content);
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

    private String generateDescriptedErrorpage(int status, String reasonPhrase, String content) {
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
}
