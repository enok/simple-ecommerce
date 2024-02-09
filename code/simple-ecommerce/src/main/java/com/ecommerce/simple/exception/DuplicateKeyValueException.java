package com.ecommerce.simple.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DuplicateKeyValueException extends RuntimeException {
    public DuplicateKeyValueException(final String message) {
        super(message);
    }
}
