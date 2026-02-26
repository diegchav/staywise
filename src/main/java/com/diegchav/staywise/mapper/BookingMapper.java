package com.diegchav.staywise.mapper;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.domain.Booking;

import java.math.BigDecimal;
import java.util.UUID;

public class BookingMapper {
    private BookingMapper() {}

    public static BookingResponse fromEntity(
            Booking booking,
            UUID hotelId,
            UUID roomTypeId
    ) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                hotelId,
                roomTypeId,
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getStatus().toString(),
                booking.getTotalPrice(),
                booking.getCreatedAt()
        );
    }

    public static Booking toEntity(
            CreateBookingRequest request,
            BigDecimal totalPrice
    ) {
        return new Booking(
                request.userId(),
                request.roomTypeId(),
                request.hotelId(),
                request.checkIn(),
                request.checkOut(),
                totalPrice
        );
    }
}
