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

package com.alesharik.webserver.configuration.config.lang.parser;

import java.util.List;

public final class CodeParsingException extends ParserException {
    private static final long serialVersionUID = 4987318662240251663L;
    private final int line;
    private final List<String> file;

    public CodeParsingException(String message, int line, List<String> file) {
        super(message);
        this.line = line - 1;
        this.file = file;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getMessage());
        builder.append('\n');
        for(int i = (Constants.VERBOSE ? 0 : line - 2); i < (Constants.VERBOSE ? file.size() : line + 3); i++) {
            if(i < 0 || i >= file.size())
                continue;
            builder.append(i);
            builder.append(" | ");
            builder.append(file.get(i));
            if(i == line)
                builder.append(" <<<< ERROR");
            builder.append('\n');
        }
        return builder.toString();
    }
}
