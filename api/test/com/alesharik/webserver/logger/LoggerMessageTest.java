package com.alesharik.webserver.logger;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class LoggerMessageTest {
    private static final String debugPrefix = "123456";

    @Test
    public void getClassPrefixWithoutAnnotation() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test1.class, debugPrefix);
        assertEquals(debugPrefix, message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithValue() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test2.class, debugPrefix);
        assertEquals("[Test]", message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithValueArray() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test3.class, debugPrefix);
        assertEquals("[Asd][Sdf]", message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithDebugModeAndValue() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test4.class, debugPrefix);
        assertEquals("[QWERTY]" + debugPrefix, message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithDebugModeAndValueArray() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test5.class, debugPrefix);
        assertEquals("[zxc][cvb]" + debugPrefix, message.getClassPrefix());
    }

    private static final class Test1 {

    }

    @Prefixes("[Test]")
    private static final class Test2 {

    }

    @Prefixes({"[Asd]", "[Sdf]"})
    private static final class Test3 {

    }

    @Prefixes(value = "[QWERTY]", requireDebugPrefix = true)
    private static final class Test4 {

    }

    @Prefixes(value = {"[zxc]", "[cvb]"}, requireDebugPrefix = true)
    private static final class Test5 {

    }
}