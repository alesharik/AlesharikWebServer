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

import com.alesharik.webserver.exception.ExceptionWithoutStacktrace;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.stream.Stream;

import static junit.framework.Assert.assertTrue;

public class BasicErrorPageConstructorTest {
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

    private static String exceptWithDescription = "<html>" +
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

    private static String exceptWithException = "<html>" +
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

    private static String exceptWithDescriptionAndException = "<html>" +
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

    private static BasicErrorPageConstructor errorPageConstructor;

    @BeforeClass
    public static void setUp() throws Exception {
        errorPageConstructor = new BasicErrorPageConstructor();
    }

    @Test
    public void generateBasic() throws Exception {
        String generated = errorPageConstructor.generate(Request.create(), 500, "Internal server error", null, null);
        assertTrue(exceptedBasic.equals(generated));
    }

    @Test
    public void generateWithDescription() throws Exception {
        String generated = errorPageConstructor.generate(Request.create(), 500, "Internal server error", "Oops!", null);
        assertTrue(exceptWithDescription.equals(generated));
    }

    @Test
    public void generateWithException() throws Exception {
        String generated = errorPageConstructor.generate(Request.create(), 500, "Internal server error", null, new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(exceptWithException.equals(generated));
    }

    @Test
    public void generateWithDescriptionAndException() throws Exception {
        String generated = errorPageConstructor.generate(Request.create(), 500, "Internal server error", "Oops!", new ExceptionWithoutStacktrace("Oops!"));
        assertTrue(exceptWithDescriptionAndException.equals(generated));
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
            assertTrue(errorPageConstructor.support(code));
        }
    }

    @Test
    public void getName() throws Exception {
        assertTrue("Basic error page generator".equals(errorPageConstructor.getName()));
    }

}