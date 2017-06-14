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

package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.exceptions.ExceptionWithoutStacktrace;
import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;

import static junit.framework.Assert.assertTrue;

public class FileBasedErrorPageConstructorTest {
    private static FileBasedErrorPageConstructor constructor;

    private static String exceptedBasic = "<html>" +
            "<head>" +
            "<style>" +
            "h1, p {" +
            "   text-align: center;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<h1>" +
            500 + " " + "Internal server error" +
            "</h1>" +
            "<hr/>" +
            "<p>AlesharikWebServer</p>" +
            "</body>" +
            "</html>";

    @BeforeClass
    public static void setUp() throws Exception {
        Logger.setupLogger(File.createTempFile("asdfsdfasfsdf", "sdfasfsdfasd"), 10);

        File tmpFolder = Files.createTempDirectory("FileBasedErrorPageConstructorTest").toFile();
        File errorFolder = new File(tmpFolder + "/errors/");
        if(!errorFolder.mkdir()) {
            throw new Error();
        }
        File test404 = new File(errorFolder + "/404.html");
        if(!test404.createNewFile()) {
            throw new Error();
        }
        Files.write(test404.toPath(), "asd".getBytes(Charsets.ASCII_CHARSET));

        File test403 = new File(errorFolder + "/403.html");
        if(!test403.createNewFile()) {
            throw new Error();
        }
        Files.write(test403.toPath(), "<div id=\"description\"></div>".getBytes(Charsets.ASCII_CHARSET));

        FileManager fileManager = new FileManager(tmpFolder, FileManager.FileHoldingMode.NO_HOLD);
        constructor = new FileBasedErrorPageConstructor(fileManager);
    }

    @Test
    public void generateFromFile() throws Exception {
        String gen = constructor.generate(Request.create(), 404, "Not found", null, null);
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  asd\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateFromFileWithDescription() throws Exception {
        String gen = constructor.generate(Request.create(), 403, "Access denied", "test", null);
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <div id=\"description\">\n" +
                "   test\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateFromFileWithException() throws Exception {
        String gen = constructor.generate(Request.create(), 403, "Access denied", null, new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <div id=\"description\">\n" +
                "   ExceptionWithoutStacktrace: Oops!\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateFromFileWithDescriptionAndException() throws Exception {
        String gen = constructor.generate(Request.create(), 403, "Access denied", "test", new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <div id=\"description\">\n" +
                "   test ExceptionWithoutStacktrace: Oops!\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateFromFileWithoutDescDivWithDescription() throws Exception {
        String gen = constructor.generate(Request.create(), 404, "Not found", "zsdfc", null);
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  asd\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateFromBasic() throws Exception {
        String gen = constructor.generate(Request.create(), 500, "Internal server error", null, null);
        assertTrue(gen.equals(exceptedBasic));
    }

    @Test
    public void support() throws Exception {
        ArrayList<Integer> codes = new ArrayList<>();
        Stream.of(HttpStatus.class.getDeclaredFields())
                .filter(field -> (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                .filter(field -> (field.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
                .forEach(field -> {
                    try {
                        codes.add(((HttpStatus) field.get(null)).getStatusCode());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
        for(int code : codes) {
            assertTrue(constructor.support(code));
        }
    }

    @Test
    public void getName() throws Exception {
        assertTrue("File-based error page generator".equals(constructor.getName()));
    }

}