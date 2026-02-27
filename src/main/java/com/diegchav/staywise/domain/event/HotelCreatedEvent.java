package com.diegchav.staywise.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HotelCreatedEvent(
        UUID eventId,
        UUID hotelId,
        String name,
        String address,
        String city,
        String country,
        BigDecimal rating,
        Instant occurredAt,
        int version
) {}
