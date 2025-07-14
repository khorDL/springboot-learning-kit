package com.springboot.learning.kit.exception;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String message) {
        super(message);
    }
}
