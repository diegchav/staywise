package com.diegchav.staywise.api.dto;

public record SearchResponse(
        String hotelId,
        String name,
        String city,
        String country,
        Double rating
) {}
