package com.alesharik.webserver.module.server;

import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

public interface SecuredStoreModule extends Module {
    void storeString(@Nonnull SecuredStoreAccessController controller, @Nonnull String name) throws IllegalAccessException;

    /**
     * @return password or empty string if password not exists
     */
    @Nonnull
    String readString(@Nonnull String name) throws IllegalAccessException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException;

    void writeString(@Nonnull String name, @Nonnull String password) throws IllegalAccessException, InvalidKeySpecException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException;
}
