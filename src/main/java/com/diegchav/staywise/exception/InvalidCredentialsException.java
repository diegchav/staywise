package com.diegchav.staywise.exception;

public class InvalidCredentialsException extends IllegalArgumentException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
