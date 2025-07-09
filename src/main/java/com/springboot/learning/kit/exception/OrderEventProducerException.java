package com.springboot.learning.kit.exception;

public class OrderEventProducerException extends RuntimeException {
    public OrderEventProducerException(String message, Exception e) {
        super(message);
    }
}
