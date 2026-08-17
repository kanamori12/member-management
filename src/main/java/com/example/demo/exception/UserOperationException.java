package com.example.demo.exception;

public class UserOperationException extends RuntimeException {

    public UserOperationException(String message) {
        super(message);
    }
}