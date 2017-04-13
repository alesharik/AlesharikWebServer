package com.alesharik.webserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown by {@link com.alesharik.webserver.api.MIMETypes} class. Means that file extension already defined
 */
@AllArgsConstructor
@Getter
public class MIMETypeAlreadyExistsException extends RuntimeException {
    private final String mimeType;
    private final String fileExtension;
}
