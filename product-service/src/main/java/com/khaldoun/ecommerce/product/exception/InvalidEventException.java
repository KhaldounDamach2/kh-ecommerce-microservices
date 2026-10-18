package com.khaldoun.ecommerce.product.exception;

// A Kafka event that can never be processed (missing id, broken envelope).
// Retrying cannot fix bad data, so the error handler sends it straight to the DLT.
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
