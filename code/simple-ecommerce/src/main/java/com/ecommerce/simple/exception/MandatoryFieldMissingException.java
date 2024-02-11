package com.ecommerce.simple.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MandatoryFieldMissingException extends RuntimeException {
    public MandatoryFieldMissingException(final String message) {
        super(message);
    }
}
