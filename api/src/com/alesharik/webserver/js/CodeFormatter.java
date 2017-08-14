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
