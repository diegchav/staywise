package com.diegchav.staywise.exception;

public class IdempotencyProcessingException extends RuntimeException {
    public IdempotencyProcessingException(String message) {
        super(message);
    }
}
