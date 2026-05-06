package com.diegchav.staywise.exception;

public class UserAlreadyTakenException extends IllegalArgumentException {
    public UserAlreadyTakenException(String message) {
        super(message);
    }
}
