package com.alesharik.webserver.js;

public final class CodeFormatter {
    public static final String JS_SPACE_REGEXP = "(?!((^(?=\"|')$).*(^(?=\").$)))(\\s)";
    //TODO write space regexp
    public static final String JS_NEXT_LINE_REGEXP = "";

    public static final String ONE_LINE_COMMENT_REGEXP = "//.*\n";
    /**
     * WARNING: need one-line string. Use it in string with no \n or with s regexp flag
     */
    public static final String MULTI_LINES_COMMENT_REGEXP = "/\\*.*\\*/";

    public static final String MULTI_LINES_DOCS_REGEXP = "/\\*\\*.*\\*/";

    private CodeFormatter() {
    }

    /**
     * Remove all comments, spaces and tabs form code
     *
     * @param code code to minimize
     * @return minimized code
     */
    public static String minimize(String code, String spaceRegexp, String nextLineRegexp) {
        return code.replace("\t", "    ")
                .replace(spaceRegexp, "")
                .replaceAll(ONE_LINE_COMMENT_REGEXP, "")
                .replaceAll(nextLineRegexp, "")
                .replaceAll(MULTI_LINES_COMMENT_REGEXP, "")
                .replaceAll(MULTI_LINES_DOCS_REGEXP, "");
    }

}
