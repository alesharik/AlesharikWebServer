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

package com.alesharik.webserver.control.dataStorage;

import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.logger.Logger;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class used for encrypt properties with specific key
 */
final class EncryptedPropertiesWriter extends PropertiesConfiguration.PropertiesWriter {
    private String key;

    /**
     * Creates a new instance of {@code PropertiesWriter}.
     *
     * @param writer     a Writer object providing the underlying stream
     * @param delHandler the delimiter handler for dealing with properties
     */
    public EncryptedPropertiesWriter(Writer writer, ListDelimiterHandler delHandler) {
        super(writer, delHandler);
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void writeln(String s) throws IOException {
        try {
            if(s != null) {
                write(StringCipher.encrypt(s, key));
            }
        } catch (InvalidKeyException | BadPaddingException | InvalidKeySpecException | IllegalBlockSizeException e) {
            Logger.log(e);
        }
        write(getLineSeparator());
    }
}
