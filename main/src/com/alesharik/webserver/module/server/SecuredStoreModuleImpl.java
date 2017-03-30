package com.alesharik.webserver.module.server;

import com.alesharik.webserver.api.StringCipher;
import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.logger.Prefix;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.utils.Charsets;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Prefix("[SecuredStoreModule]")
public class SecuredStoreModuleImpl implements SecuredStoreModule {
    private final Map<String, SecuredStoreAccessController> accessControllers;
    private File store;

    public SecuredStoreModuleImpl() {
        accessControllers = new ConcurrentHashMap<>();
    }

    @Override
    public void storeString(@Nonnull SecuredStoreAccessController controller, @Nonnull String name) throws IllegalAccessException {
        if(accessControllers.containsKey(name)) {
            throw new IllegalAccessException();
        }
        accessControllers.put(name, controller);
    }

    @Override
    public String readString(@Nonnull String name) throws IllegalAccessException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if(name.contains(": ")) {
            throw new IllegalArgumentException("Name contains `: `!");
        }
        SecuredStoreAccessController accessController = accessControllers.get(name);
        if(accessController == null) {
            throw new IllegalArgumentException("name");
        }
        Class<?> clazz = CallingClass.INSTANCE.getCallingClasses()[2];
        if(!accessController.grantAccess(clazz)) {
            throw new IllegalAccessException();
        }

        SecretKey secretKey = getSecretKey();
        List<String> lines = Files.readAllLines(store.toPath(), Charsets.UTF8_CHARSET);
        for(String s : lines.subList(1, lines.size())) {
            String decrypted = StringCipher.decrypt(s, null, secretKey);
            if(decrypted.startsWith(name + ": ")) {
                String value = decrypted.substring(name.concat(": ").length());
                return StringCipher.decrypt(value, null, accessController.passwordKey());
            }
        }
        return "";
    }

    @Override
    public void writeString(String name, String value) throws IllegalAccessException, InvalidKeySpecException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        if(name.contains(": ")) {
            throw new IllegalArgumentException("Name contains `: `!");
        }
        SecuredStoreAccessController accessController = accessControllers.get(name);
        if(accessController == null) {
            throw new IllegalArgumentException("name");
        }
        Class<?> clazz = CallingClass.INSTANCE.getCallingClasses()[2];
        if(!accessController.grantAccess(clazz)) {
            throw new IllegalAccessException();
        }

        SecretKey secretKey = getSecretKey();
        List<String> lines = Files.readAllLines(store.toPath(), Charsets.UTF8_CHARSET);
        for(String s : lines.subList(1, lines.size())) {
            String decrypted = StringCipher.decrypt(s, null, secretKey);
            if(decrypted.startsWith(name + ": ")) {
                String n = decrypted.substring(0, name.concat(": ").length()).concat(StringCipher.encrypt(value, null, accessController.passwordKey()));
                lines.replaceAll(s1 -> {
                    if(s1.equals(s)) {
                        return n;
                    } else {
                        return s1;
                    }
                });
                break;
            }
        }
        Optional<String> s3 = lines.subList(1, lines.size())
                .stream()
                .map(s -> {
                    try {
                        return StringCipher.encrypt(s, null, secretKey);
                    } catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                    return "";
                })
                .reduce((s, s2) -> s.concat("\n").concat("s2"));
        String w = lines.get(0).concat("\n").concat(s3.orElse(""));
    }

    @Override
    public void parse(@Nullable Element configNode) {
        Element node = (Element) configNode.getElementsByTagName("store").item(0);
        if(node == null) {
            throw new ConfigurationParseError("Secured store file node not found!");
        } else {
            store = new File(node.getTextContent());
        }
        try {
            if((!store.exists() && !store.createNewFile()) || store.isDirectory() || !store.canRead() || !store.canWrite()) {
                throw new ConfigurationParseError("Secured store file error!");
            }
        } catch (IOException e) {
            throw new ConfigurationParseError(e);
        }
    }

    @Override
    public void reload(@Nullable Element configNode) {
        Element node = (Element) configNode.getElementsByTagName("store").item(0);
        if(node == null) {
            throw new ConfigurationParseError("Secured store file node not found!");
        } else {
            store = new File(node.getTextContent());
        }
        try {
            if((!store.exists() && !store.createNewFile()) || store.isDirectory() || !store.canRead() || !store.canWrite()) {
                throw new ConfigurationParseError("Secured store file error!");
            }
        } catch (IOException e) {
            throw new ConfigurationParseError(e);
        }
    }

    private SecretKey getSecretKey() throws IOException, InvalidKeySpecException, InvalidKeyException {
        List<String> strings = Files.readAllLines(store.toPath(), Charsets.UTF8_CHARSET);
        if(strings.isEmpty()) {
            writeNewKey();
            return getSecretKey();
        }
        byte[] data = strings.get(0).getBytes(Charsets.UTF8_CHARSET);
        return new SecretKeySpec(Base64Utils.decode(new String(data, Charsets.UTF8_CHARSET)), StringCipher.DESEDE_ENCRYPTION_SCHEME);
    }

    private void writeNewKey() throws InvalidKeySpecException, InvalidKeyException, IOException {
        String k = Utils.getRandomString(24);
        SecretKey secretKey = StringCipher.generateKey(k);
        Files.write(store.toPath(), Base64Utils.encodeToString(secretKey.getEncoded(), false).concat("\n").getBytes(Charsets.UTF8_CHARSET), StandardOpenOption.CREATE);
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void shutdownNow() {
    }

    @Nonnull
    @Override
    public String getName() {
        return "server-password-store";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }


    private static class CallingClass extends SecurityManager {
        public static final CallingClass INSTANCE = new CallingClass();

        private CallingClass() {
        }

        public Class[] getCallingClasses() {
            return getClassContext();
        }
    }

}
