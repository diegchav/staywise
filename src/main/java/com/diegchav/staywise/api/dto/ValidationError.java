package com.diegchav.staywise.api.dto;

public record ValidationError(
        String field,
        String error
) {}
