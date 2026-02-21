package com.diegchav.staywise.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID userId,
        UUID hotelId,
        UUID roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        String status,
        BigDecimal totalPrice,
        Instant createdAt
) {}
