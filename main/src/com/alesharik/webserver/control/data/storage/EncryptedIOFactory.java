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

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

import java.io.Reader;
import java.io.Writer;

/**
 * This class used for add {@link EncryptedPropertiesReader} and {@link EncryptedPropertiesWriter} to configuration
 */
@Deprecated
final class EncryptedIOFactory extends PropertiesConfiguration.DefaultIOFactory {
    private String key;

    public EncryptedIOFactory(String key) {
        this.key = key;
    }

    @Override
    public PropertiesConfiguration.PropertiesWriter createPropertiesWriter(Writer out, ListDelimiterHandler handler) {
        EncryptedPropertiesWriter writer = new EncryptedPropertiesWriter(out, handler);
        writer.setKey(key);
        return writer;
    }

    @Override
    public PropertiesConfiguration.PropertiesReader createPropertiesReader(Reader in) {
        EncryptedPropertiesReader reader = new EncryptedPropertiesReader(in);
        reader.setKey(key);
        return reader;
    }
}
