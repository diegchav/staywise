package com.diegchav.staywise.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull
        UUID userId, // TODO: Do not pass user id on the request

        @NotNull
        UUID hotelId,

        @NotNull
        UUID roomTypeId,

        @NotNull
        LocalDate checkIn,

        @NotNull
        LocalDate checkOut
) {}
