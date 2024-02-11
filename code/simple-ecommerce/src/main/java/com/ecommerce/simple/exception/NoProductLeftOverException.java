package com.ecommerce.simple.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoProductLeftOverException extends RuntimeException {
    public NoProductLeftOverException(final String message) {
        super(message);
    }
}
