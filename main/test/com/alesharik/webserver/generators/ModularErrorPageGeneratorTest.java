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

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.exception.ExceptionWithoutStacktrace;
import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ModularErrorPageGeneratorTest {
    private static ModularErrorPageGenerator generator;

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

    private static String exceptBasicWithDescription = "<html>" +
            "<head>" +
            "<style>" +
            "h1, p, pre {" +
            "   text-align: center;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<h1>" +
            500 + " " + "Internal server error" +
            "</h1>" +
            "<pre>" +
            "Oops!" + //Content
            "</pre>" +
            "<hr/>" +
            "<p>AlesharikWebServer</p>" +
            "</body>" +
            "</html>";

    private static String exceptBasicWithException = "<html>" +
            "<head>" +
            "<style>" +
            "h1, p, pre {" +
            "   text-align: center;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<h1>" +
            500 + " " + "Internal server error" +
            "</h1>" +
            "<pre>" +
            "ExceptionWithoutStacktrace: Oops!" + //Exception
            "</pre>" +
            "<hr/>" +
            "<p>AlesharikWebServer</p>" +
            "</body>" +
            "</html>";

    private static String exceptBasicWithDescriptionAndException = "<html>" +
            "<head>" +
            "<style>" +
            "h1, p, pre {" +
            "   text-align: center;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<h1>" +
            500 + " " + "Internal server error" +
            "</h1>" +
            "<pre>" +
            "Oops!\n" + //Content
            "ExceptionWithoutStacktrace: Oops!" + //Exception
            "</pre>" +
            "<hr/>" +
            "<p>AlesharikWebServer</p>" +
            "</body>" +
            "</html>";

    private static ErrorPageConstructor custom = new ErrorPageConstructor() {
        @Override
        public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
            return "hi!";
        }

        @Override
        public boolean support(int status) {
            return Integer.compare(status, 501) == 0;
        }

        @Override
        public String getName() {
            return "hi!";
        }
    };

    private static ErrorPageConstructor toRemove = new ErrorPageConstructor() {
        @Override
        public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
            return "hi!";
        }

        @Override
        public boolean support(int status) {
            return Integer.compare(status, 502) == 0;
        }

        @Override
        public String getName() {
            return "hi!";
        }
    };

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
        generator = new ModularErrorPageGenerator(fileManager);
        generator.addErrorPageConstructor(custom);
        generator.addErrorPageConstructor(toRemove);
    }

    //Check if all works
    @Test
    public void generateWithBasic() throws Exception {
        String generated = generator.generate(Request.create(), 500, "Internal server error");
        assertTrue(exceptedBasic.equals(generated));
    }

    @Test
    public void generateWithBasicWithDescription() throws Exception {
        String generated = generator.generate(Request.create(), 500, "Internal server error", "Oops!");
        assertTrue(exceptBasicWithDescription.equals(generated));
    }

    @Test
    public void generateWithBasicWithException() throws Exception {
        String generated = generator.generate(Request.create(), 500, "Internal server error", null, new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(exceptBasicWithException.equals(generated));
    }

    @Test
    public void generateWithBasicWithDescriptionAndException() throws Exception {
        String generated = generator.generate(Request.create(), 500, "Internal server error", "Oops!", new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(exceptBasicWithDescriptionAndException.equals(generated));
    }

    @Test
    public void generateWithFileBased() throws Exception {
        String gen = generator.generate(Request.create(), 404, "Not found");
        assertTrue(gen.equals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  asd\n" +
                " </body>\n" +
                "</html>"));
    }

    @Test
    public void generateWithFileBasedWithDescription() throws Exception {
        String gen = generator.generate(Request.create(), 403, "Access denied", "test");
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
    public void generateWithFileBasedWithException() throws Exception {
        String gen = generator.generate(Request.create(), 403, "Access denied", null, new ExceptionWithoutStacktrace("Oops!"));
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
    public void generateWithFileBasedWithDescriptionAndException() throws Exception {
        String gen = generator.generate(Request.create(), 403, "Access denied", "test", new ExceptionWithoutStacktrace("Oops!"));
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
    public void generateWithCustom() throws Exception {
        String gen = generator.generate(Request.create(), 501, "Test");
        assertTrue(gen.equals("hi!"));
    }

    @Test
    public void addErrorPageConstructor() throws Exception {
        generator.addErrorPageConstructor(new ErrorPageConstructor() {
            @Override
            public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
                return null;
            }

            @Override
            public boolean support(int status) {
                return false;
            }

            @Override
            public String getName() {
                return null;
            }
        });
    }

    @Test
    public void removeErrorPageConstructor() throws Exception {
        generator.removeErrorPageConstructor(toRemove);
        assertFalse(generator.containsErrorPageConstructor(toRemove));
    }

    @Test
    public void containsErrorPageConstructor() throws Exception {
        assertFalse(generator.containsErrorPageConstructor(null));
        assertTrue(generator.containsErrorPageConstructor(custom));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getErrorPageConstructorsForStatus() throws Exception {
        List<ErrorPageConstructor> constructors = generator.getErrorPageConstructorsForStatus(200);
        assertNotNull(constructors);
        constructors.add(new ErrorPageConstructor() {
            @Override
            public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
                return null;
            }

            @Override
            public boolean support(int status) {
                return false;
            }

            @Override
            public String getName() {
                return null;
            }
        });
    }

    @Test
    public void setDefaultErrorPageConstructor() throws Exception {
        generator.setDefaultErrorPageConstructor(custom, 501);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDefaultErrorPageConstructorIllegalArgumentException() throws Exception {
        generator.setDefaultErrorPageConstructor(custom, 502);
    }
}