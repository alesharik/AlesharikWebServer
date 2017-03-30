package com.alesharik.webserver.module.server;

import javax.crypto.SecretKey;

public interface SecuredStoreAccessController {
    boolean grantAccess(Object instance);

    /**
     * Must be same all load cycles
     */
    SecretKey passwordKey();
}
