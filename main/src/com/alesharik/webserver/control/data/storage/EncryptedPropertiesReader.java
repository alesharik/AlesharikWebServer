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

package com.alesharik.webserver.control.data.storage;

import com.alesharik.webserver.api.utils.crypto.StringCipher;
import com.alesharik.webserver.logger.Logger;
import org.apache.commons.configuration2.PropertiesConfiguration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class decrypt properties using specific key
 */
@Deprecated
final class EncryptedPropertiesReader extends PropertiesConfiguration.PropertiesReader {
    private static final String COMMENT_CHARS = "#!";
    private String key;

    /**
     * Constructor.
     *
     * @param reader A Reader.
     */
    public EncryptedPropertiesReader(Reader reader) {
        super(reader);
    }

    private static boolean isCommentLine(String line) {
        String s = line.trim();
        return s.length() < 1 || COMMENT_CHARS.indexOf(s.charAt(0)) >= 0;
    }

    private static boolean checkCombineLines(String line) {
        return countTrailingBS(line) % 2 != 0;
    }

    private static int countTrailingBS(String line) {
        int bsCount = 0;
        for(int idx = line.length() - 1; idx >= 0 && line.charAt(idx) == '\\'; idx--) {
            bsCount++;
        }
        return bsCount;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String readProperty() throws IOException {
        try {
            StringBuilder buffer = new StringBuilder();
            while(true) {
                String line = readLine();
                if(line == null) {
                    return null;
                }
                if(isCommentLine(line)) {
                    continue;
                }

                line = line.trim();
                if(checkCombineLines(line)) {
                    line = line.substring(0, line.length() - 1);
                    buffer.append(StringCipher.decrypt(line, key));
                } else {
                    buffer.append(StringCipher.decrypt(line, key));
                    break;
                }
            }
            return buffer.toString();
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidKeyException e) {
            Logger.log(e);
        }
        return "";
    }
}
