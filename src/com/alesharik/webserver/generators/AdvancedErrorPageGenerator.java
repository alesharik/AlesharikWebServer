package com.alesharik.webserver.generators;

import com.alesharik.webserver.main.FileManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This class used for generate error pages or use custom.<br>
 * All custom pages must locate in <code>{fileMangerRoot}/errors/</code><br>
 * Custom error page must be named as error code, which customize error page<br>
 * For use error pages with description in custom error page file must be element with <code>id="description"</code>
 */
public final class AdvancedErrorPageGenerator implements ErrorPageGenerator {
    private static final BasicErrorPageGenerator BASIC_ERROR_PAGE_GENERATOR = new BasicErrorPageGenerator();

    private final FileManager fileManager;

    public AdvancedErrorPageGenerator(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        if(fileManager.exists("/errors/" + status + ".html", true)) {
            String file = new String(fileManager.readFile("/errors/" + status + ".html"));
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
                return file;
            } else {
                return generateDescriptedErrorpage(status, reasonPhrase, content, file);
            }
        } else {
            return BASIC_ERROR_PAGE_GENERATOR.generate(request, status, reasonPhrase, description, exception);
        }
    }

    private String generateDescriptedErrorpage(int status, String reasonPhrase, String content, String file) {
        Document document = Jsoup.parse(file);
        Elements elements = document.select("#description");
        elements.first().replaceWith(elements.first().html(content));
        return document.html();
    }
}
