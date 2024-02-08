package com.ecommerce.simple.exception;

import com.ecommerce.simple.model.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private List<String> getErrors(Set<ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 400
     */
    @ResponseStatus(BAD_REQUEST)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error("[ handleHttpMessageNotReadable ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(BAD_REQUEST, ex), BAD_REQUEST);
    }

    /**
     * 404
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("[ handleConstraintViolationException ] {}", ex.getMessage());
        List<String> errors = getErrors(ex.getConstraintViolations());
        return new ResponseEntity<>(new Error(BAD_REQUEST, errors), BAD_REQUEST);
    }

    /**
     * 404
     */
    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        log.error("[ handleNotFoundException ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(NOT_FOUND, ex), NOT_FOUND);
    }
}
