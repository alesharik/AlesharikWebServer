package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import com.alesharik.webserver.main.FileManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.utils.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This class used for generate error pages or use custom.<br>
 * All custom pages must locate in <code>{fileMangerRoot}/errors/</code><br>
 * Custom error page must be named as error code, which customize error page<br>
 * For use error pages with description in custom error page file must be element with <code>id="description"</code>
 */
public final class FileBasedErrorPageConstructor implements ErrorPageConstructor {
    private static final BasicErrorPageConstructor BASIC_ERROR_PAGE_CONSTRUCTOR = new BasicErrorPageConstructor();

    private final FileManager fileManager;

    public FileBasedErrorPageConstructor(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        if(fileManager.exists("/errors/" + status + ".html", true)) {
            String file = new String(fileManager.readFile("/errors/" + status + ".html"), Charsets.UTF8_CHARSET);
            StringBuilder content = new StringBuilder();
            if(description != null && !description.isEmpty()) {
                content.append(description);
            }
            if(exception != null) {
                if(content.length() > 0) {
                    content.append('\n');
                }
                content.append(ExceptionUtils.getMessage(exception));
            }

            if(content.length() <= 0) {
                return Jsoup.parse(file).html();
            } else {
                return generateErrorPageWithDescription(content.toString(), file);
            }
        } else {
            return BASIC_ERROR_PAGE_CONSTRUCTOR.generate(request, status, reasonPhrase, description, exception);
        }
    }

    private String generateErrorPageWithDescription(String content, String file) {
        Document document = Jsoup.parse(file);
        Elements elements = document.select("#description");
        if(elements.first() == null) {
            return document.html();
        }
        elements.first().html(content);
        return document.html();
    }

    @Override
    public boolean support(int status) {
        return true;
    }

    @Override
    public String getName() {
        return "File-based error page generator";
    }
}
