package com.ecommerce.simple.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class Error {
    private final Integer httpCode;
    private final String message;
    private final String detailedMessage;

    public Error(final HttpStatus httpStatus, Exception exception) {
        this.httpCode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.detailedMessage = cleanUpMessage(exception.getMessage());
    }

    public Error(final HttpStatus httpStatus, List<String> errors) {
        this.httpCode = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.detailedMessage = errors.toString();
    }

    private String cleanUpMessage(final String message) {
        return message
                .replaceAll("public com\\.ecommerce\\.simple.*", "")
                .replaceAll(" for \\[class com\\.ecommerce\\.simple.*\\]", "");
    }
}
