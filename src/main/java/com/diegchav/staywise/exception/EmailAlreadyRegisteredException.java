package com.diegchav.staywise.exception;

public class EmailAlreadyRegisteredException extends IllegalArgumentException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
