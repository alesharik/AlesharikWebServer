package com.alesharik.webserver.js;

import junit.framework.Assert;
import org.junit.Test;

public class CodeFormatterTest extends Assert {

    @Test
    public void minimize() {
        String code = "one two \"three four\"";
        System.out.println(CodeFormatter.minimize(code, CodeFormatter.JS_SPACE_REGEXP, "\n"));

    }
}