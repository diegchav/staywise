package com.diegchav.staywise.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HotelResponse(
    UUID id,
    String name,
    String address,
    String city,
    String country,
    BigDecimal rating
) {}
