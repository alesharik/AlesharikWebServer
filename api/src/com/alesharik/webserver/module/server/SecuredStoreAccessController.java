package com.alesharik.webserver.module.server;

import javax.crypto.SecretKey;

public interface SecuredStoreAccessController {
    boolean grantAccess(Class<?> clazz);

    /**
     * Must be same all load cycles
     */
    SecretKey passwordKey();
}
