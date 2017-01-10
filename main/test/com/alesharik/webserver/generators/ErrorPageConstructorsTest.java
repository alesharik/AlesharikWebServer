package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;
import org.glassfish.grizzly.http.server.Request;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ErrorPageConstructorsTest {
    private static ErrorPageConstructors constructors;

    private static ErrorPageConstructor toRemove = new ErrorPageConstructor() {
        @Override
        public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
            return "hello1";
        }

        @Override
        public boolean support(int status) {
            return true;
        }

        @Override
        public String getName() {
            return "hello1";
        }
    };

    private static ErrorPageConstructor hello = new ErrorPageConstructor() {
        @Override
        public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
            return "hello";
        }

        @Override
        public boolean support(int status) {
            return true;
        }

        @Override
        public String getName() {
            return "hello";
        }
    };

    @BeforeClass
    public static void setUp() throws Exception {
        constructors = new ErrorPageConstructors();
        constructors.addConstructor(hello);
        constructors.addConstructor(toRemove);
    }

    @Test
    public void addConstructor() throws Exception {
        constructors.addConstructor(new ErrorPageConstructor() {
            @Override
            public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
                return "hi!";
            }

            @Override
            public boolean support(int status) {
                return Integer.compare(status, 403) == 0;
            }

            @Override
            public String getName() {
                return "hi!";
            }
        });
    }

    @Test
    public void removeErrorPageConstructor() throws Exception {
        constructors.removeErrorPageConstructor(toRemove);
        assertFalse(constructors.containsConstructor(toRemove));
    }

    @Test
    public void containsConstructor() throws Exception {
        assertFalse(constructors.containsConstructor(new ErrorPageConstructor() {
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
        }));
        assertTrue(constructors.containsConstructor(hello));
    }

    @Test
    public void setDefault() throws Exception {
        ErrorPageConstructor test = new ErrorPageConstructor() {
            @Override
            public String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable) {
                return "test";
            }

            @Override
            public boolean support(int status) {
                return Integer.compare(status, 400) == 0;
            }

            @Override
            public String getName() {
                return null;
            }
        };
        constructors.addConstructor(test);
        constructors.setDefault(test, 400);
        assertTrue(constructors.getConstructor(400).orElse(new ErrorPageConstructor() {
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
        }).equals(test));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDefaultIllegalArgument() throws Exception {
        constructors.setDefault(new ErrorPageConstructor() {
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
        }, 300);
    }

    @Test
    public void getConstructor() throws Exception {
        assertNotNull(constructors.getConstructor(200));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructors() throws Exception {
        List<ErrorPageConstructor> constructors = ErrorPageConstructorsTest.constructors.constructors(200);
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
}