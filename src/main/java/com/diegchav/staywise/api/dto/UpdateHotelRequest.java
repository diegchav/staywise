package com.diegchav.staywise.api.dto;

public record UpdateHotelRequest(
        String name,
        String address,
        String city,
        String country
) {}
