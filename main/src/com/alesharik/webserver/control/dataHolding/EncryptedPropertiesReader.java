package com.alesharik.webserver.control.dataHolding;

import com.alesharik.webserver.api.StringCipher;
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
