package com.ecommerce.simple.exception;

import com.ecommerce.simple.model.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.ServiceUnavailableException;
import java.net.ConnectException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 400
     */
    @NonNull
    @ResponseStatus(BAD_REQUEST)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error("[ handleHttpMessageNotReadable ] {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new Error(BAD_REQUEST, ex));
    }

    /**
     * 400
     */
    @NonNull
    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<Object> handleConstraintViolationException(@NonNull ConstraintViolationException ex) {
        log.error("[ handleConstraintViolationException ] {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new Error(BAD_REQUEST, getErrors(ex.getConstraintViolations())));
    }

    /**
     * 400
     */
    @NonNull
    @ExceptionHandler(value = {DuplicateKeyValueException.class, NoProductLeftOverException.class, MandatoryFieldMissingException.class})
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<Object> handleCustomBadRequestException(@NonNull Exception ex) {
        log.error("[ handleCustomBadRequestException ] {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new Error(BAD_REQUEST, ex));
    }

    /**
     * 404
     */
    @Nullable
    @ResponseStatus(NOT_FOUND)
    protected ResponseEntity<Object> handleNoResourceFoundException(@NonNull NoResourceFoundException ex,
                                                                    @NonNull HttpHeaders headers,
                                                                    @NonNull HttpStatusCode status,
                                                                    @NonNull WebRequest request) {
        log.error("[ handleNoResourceFoundException ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(NOT_FOUND, ex), NOT_FOUND);
    }

    /**
     * 404
     */
    @NonNull
    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<Object> handleNotFoundException(@NonNull NotFoundException ex) {
        log.error("[ handleNotFoundException ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(NOT_FOUND, ex), NOT_FOUND);
    }

    /**
     * 405
     */
    @Override
    @NonNull
    @ResponseStatus(METHOD_NOT_ALLOWED)
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex,
                                                                         @NonNull HttpHeaders headers,
                                                                         @NonNull HttpStatusCode status,
                                                                         @NonNull WebRequest request) {
        log.error("[ handleHttpRequestMethodNotSupported ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(METHOD_NOT_ALLOWED, ex), METHOD_NOT_ALLOWED);
    }

    /**
     * 406
     */
    @Override
    @NonNull
    @ResponseStatus(NOT_ACCEPTABLE)
    protected ResponseEntity<Object> handleHttpMessageNotWritable(@NonNull HttpMessageNotWritableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error("[ handleHttpMessageNotWritable ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(NOT_ACCEPTABLE, ex), NOT_ACCEPTABLE);
    }

    /**
     * 415
     */
    @Override
    @NonNull
    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatusCode status,
                                                                     @NonNull WebRequest request) {
        log.error("[ handleHttpMediaTypeNotSupported ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(UNSUPPORTED_MEDIA_TYPE, ex), UNSUPPORTED_MEDIA_TYPE);
    }

    private List<String> getErrors(Set<ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 500
     */
    @NonNull
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleNotMappedException(@NonNull Exception ex) {
        log.error("[ handleNotMappedException ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(INTERNAL_SERVER_ERROR, ex), INTERNAL_SERVER_ERROR);
    }

    /**
     * 503
     */
    @NonNull
    @ExceptionHandler(value = {ConnectException.class, ServiceUnavailableException.class})
    @ResponseStatus(SERVICE_UNAVAILABLE)
    public ResponseEntity<Object> handleServiceUnavailable(@NonNull Exception ex) {
        log.error("[ handleServiceUnavailable ] {}", ex.getMessage());
        return new ResponseEntity<>(new Error(SERVICE_UNAVAILABLE, ex), SERVICE_UNAVAILABLE);
    }
}
