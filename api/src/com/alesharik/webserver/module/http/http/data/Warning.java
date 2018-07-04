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

package com.alesharik.webserver.module.http.http.data;

import com.alesharik.webserver.module.http.http.HeaderManager;
import lombok.Getter;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Date;

@Getter
public class Warning {
    protected final Code code;
    protected final String agent;
    protected final String text;
    protected final Date date;

    public Warning(Code code, String agent, String text) {
        this(code, agent, text, null);
    }

    public Warning(Code code, String agent, String text, Date date) {
        this.code = code;
        this.agent = agent;
        this.text = text;
        this.date = date == null ? null : (Date) date.clone();
    }

    public boolean hasDate() {
        return date != null;
    }

    public String toHeaderString() {
        return Short.toString(code.code) + ' ' + agent + " \"" + text + (hasDate() ? "\" \"" + HeaderManager.WEB_DATE_FORMAT.get().format(date) + "\"" : "\"");
    }

    public Date getDate() {
        return date == null ? null : (Date) date.clone();
    }

    public static Warning parse(String s) {
        String[] strings = s.split(" ", 4);
        if(strings.length == 4) {
            try {
                return new Warning(Code.forCode(Short.parseShort(strings[0])), strings[1], strings[2].substring(1, strings[2].length() - 1), HeaderManager.WEB_DATE_FORMAT.get().parse(strings[3].substring(1, strings[3].length() - 1)));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new Warning(Code.forCode(Short.parseShort(strings[0])), strings[1], strings[2].substring(1, strings[2].length() - 1));
        }
    }

    public enum Code {
        CODE_110(110),
        CODE_111(111),
        CODE_112(112),
        CODE_113(113),
        CODE_199(199),
        CODE_214(214),
        CODE_299(299);

        @Getter
        private final short code;

        Code(int code) {
            this.code = (short) code;
        }

        @Nullable
        public static Code forCode(int code) {
            for(Code code1 : values()) {
                if(code1.code == (short) code)
                    return code1;
            }
            return null;
        }
    }
}
