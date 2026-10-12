package com.khaldoun.ecommerce.notification.exception;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotificationNotFoundException exception) {
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return errorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        log.error("Unhandled exception", exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message);
        return ResponseEntity.status(status).body(response);
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message) {
    }
}
