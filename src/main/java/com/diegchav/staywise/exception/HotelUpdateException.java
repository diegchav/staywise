package com.diegchav.staywise.exception;

import com.diegchav.staywise.api.dto.ValidationError;

import java.util.List;

public class HotelUpdateException extends RuntimeException {
    private final List<ValidationError> errors;

    public HotelUpdateException(List<ValidationError> errors) {
        super();

        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
