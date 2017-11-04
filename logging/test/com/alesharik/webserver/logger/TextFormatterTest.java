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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextFormatterTest {
    private static final Logger.ForegroundColor test_foreground = Logger.ForegroundColor.BLACK;
    private static final Logger.BackgroundColor test_background = Logger.BackgroundColor.BLUE;

    private Logger.TextFormatter textFormatter;

    @Before
    public void setUp() throws Exception {
        textFormatter = new Logger.TextFormatter(test_foreground, test_background);
    }

    @Test
    public void format() throws Exception {
//        assertEquals("", textFormatter.format("asd"), "\033[30;44masd"); //FIXME
    }

    @Test
    public void getForegroundColor() throws Exception {
        assertEquals(textFormatter.getForegroundColor(), test_foreground);
    }

    @Test
    public void getBackgroundColor() throws Exception {
        assertEquals(textFormatter.getBackgroundColor(), test_background);
    }
}