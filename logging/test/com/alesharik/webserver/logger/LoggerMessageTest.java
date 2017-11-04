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

package com.alesharik.webserver.logger;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class LoggerMessageTest {
    private static final String debugPrefix = "123456";

    @Test
    public void getClassPrefixWithoutAnnotation() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test1.class, debugPrefix, false);
        assertEquals(debugPrefix, message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithValue() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test2.class, debugPrefix, false);
        assertEquals("[Test]", message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithValueArray() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test3.class, debugPrefix, false);
        assertEquals("[Asd][Sdf]", message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithDebugModeAndValue() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test4.class, debugPrefix, false);
        assertEquals("[QWERTY]" + debugPrefix, message.getClassPrefix());
    }

    @Test
    public void getClassPrefixWithDebugModeAndValueArray() throws Exception {
        Logger.Message message = new Logger.Message("", "", Test5.class, debugPrefix, false);
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