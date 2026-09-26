package com.khaldoun.ecommerce.auth.exception;

public class UserNotEnabledException extends RuntimeException {

    public UserNotEnabledException(String message) {
        super(message);
    }
}