package com.alesharik.webserver.control.dataHolding;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

import java.io.Reader;
import java.io.Writer;

/**
 * This class used for add {@link EncryptedPropertiesReader} and {@link EncryptedPropertiesWriter} to configuration
 */
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
