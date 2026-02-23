package com.diegchav.staywise.exception;

public class NoInventoryException extends RuntimeException {
    public NoInventoryException(String message) {
        super(message);
    }
}
