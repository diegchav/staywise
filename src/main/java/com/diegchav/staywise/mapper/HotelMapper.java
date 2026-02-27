package com.diegchav.staywise.mapper;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.api.dto.HotelResponse;
import com.diegchav.staywise.domain.entity.Hotel;
import com.diegchav.staywise.domain.event.HotelCreatedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class HotelMapper {
    private HotelMapper() {}

    public static HotelResponse fromEntity(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getRating()
        );
    }

    public static Hotel toEntity(
            CreateHotelRequest request,
            BigDecimal rating
    ) {
        return new Hotel(
                request.name(),
                request.address(),
                request.city(),
                request.country(),
                rating
        );
    }

    public static HotelCreatedEvent toEvent(Hotel hotel) {
        return new HotelCreatedEvent(
                UUID.randomUUID(),
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getRating(),
                Instant.now(),
                1
        );
    }
}
