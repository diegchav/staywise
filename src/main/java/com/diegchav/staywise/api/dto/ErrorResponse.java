package com.diegchav.staywise.api.dto;

public record ErrorResponse(
        String error,
        int code
) {}
