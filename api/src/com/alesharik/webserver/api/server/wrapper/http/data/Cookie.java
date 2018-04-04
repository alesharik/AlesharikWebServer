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

package com.alesharik.webserver.api.server.wrapper.http.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class Cookie implements Cloneable {
    private static final String OLD_DATE;
    private static final ThreadLocal<SimpleDateFormat> OLD_COOKIE_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat;
    }); //Fix DateFormat multithreading issues

    static {
        OLD_DATE = OLD_COOKIE_DATE_FORMAT.get().format(new Date(0));
    }

    protected final String name;
    protected final String value;

    protected String comment;
    protected String domain;
    protected int maxAge = -1;
    protected String path;
    protected boolean secure;
    protected int version = 0;
    protected boolean isHttpOnly;

    public Cookie(@Nonnull String name, @Nonnull String value) {
        if(name.contains("=") || name.contains(";") || name.contains(",") || containsWhiteSpace(name))
            throw new IllegalArgumentException("Name can't have '=', ';', ',' and whitespace characters!");
        if(value.contains(";") || value.contains(",") || containsWhiteSpace(value))
            throw new IllegalArgumentException("Value can't have ';', ',' and whitespace characters!");
        this.name = name;
        this.value = value;
    }

    static boolean containsWhiteSpace(String str) {
        return str.contains(" ") || str.contains("\t") || str.contains("\n") || str.contains("\r") || str.contains("\f");
    }

    /**
     * Parse cookies from Cookie header
     *
     * @param header Cookie header line
     * @return all cookies with key-value initialized only
     */
    public static Cookie[] parseCookies(String header) {
        String[] cookies = header
                .replaceAll("[C|c]ookie:", "")
                .replace(" ", "")
                .split(";");
        Cookie[] ret = new Cookie[cookies.length];
        for(int i = 0; i < cookies.length; i++) {
            String[] str = cookies[i].split("=");
            ret[i] = new Cookie(str[0], str[1]);
        }
        return ret;
    }

    /**
     * Positive value indicates that cookie will expire after maxAge seconds. Negative value means that cookie must be deleted
     * at Web browser exits. Zero value causes cookie to be deleted
     *
     * @param maxAge cookie age in seconds
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * @param version 0 - default, 1 - RFC 2109(allows Comment and Max-Age)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Return string for Set-Cookie header
     */
    public String toCookieString() {
        return toCookieString(System.currentTimeMillis());
    }

    String toCookieString(long timeStamp) {
        StringBuilder stringBuilder = new StringBuilder(127);
        stringBuilder.append(name);//~5 chars
        stringBuilder.append('=');
        stringBuilder.append(value);//~10 chars
        //~16 chars

        if(maxAge >= 0) {
            if(version > 0) {
                stringBuilder.append("; Max-Age=");
                stringBuilder.append(maxAge);
            } else {
                stringBuilder.append("; Expires=");
                if(maxAge == 0)
                    stringBuilder.append(OLD_DATE);
                else
                    stringBuilder.append(OLD_COOKIE_DATE_FORMAT.get().format(new Date(timeStamp + maxAge * 1000L)));
            }
        }
        //~36 chars

        if(version > 0 && comment != null && !comment.isEmpty()) {
            stringBuilder.append("; Comment=");
            stringBuilder.append(comment);//~10 chars
        }

        if(domain != null && !domain.isEmpty()) {
            stringBuilder.append("; Domain=");
            stringBuilder.append(domain);//~10 chars
        }

        if(path != null && !path.isEmpty()) {
            stringBuilder.append("; Path=");
            stringBuilder.append(path);//~10 chars
        }

        if(secure)
            stringBuilder.append("; Secure");

        if(version == 1) {
            stringBuilder.append("; Version=1");
        }
        if(isHttpOnly) {
            stringBuilder.append("; HttpOnly");
        }
        //Sum = ~127
        return stringBuilder.toString();
    }

    @Override
    public Cookie clone() throws CloneNotSupportedException {
        super.clone();

        Cookie clone = new Cookie(name, value);
        clone.comment = comment;
        clone.domain = domain;
        clone.maxAge = maxAge;
        clone.path = path;
        clone.secure = secure;
        clone.version = version;
        clone.isHttpOnly = isHttpOnly;
        return clone;
    }
}
