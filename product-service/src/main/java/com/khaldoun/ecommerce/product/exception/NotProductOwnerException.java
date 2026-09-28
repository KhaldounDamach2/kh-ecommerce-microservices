package com.khaldoun.ecommerce.product.exception;

public class NotProductOwnerException extends RuntimeException {

    public NotProductOwnerException(String message) {
        super(message);
    }
}
