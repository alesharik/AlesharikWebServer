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