package com.diegchav.staywise.mapper;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.domain.Booking;
import com.diegchav.staywise.domain.Hotel;
import com.diegchav.staywise.domain.RoomType;

import java.math.BigDecimal;
import java.util.UUID;

public class BookingMapper {
    private BookingMapper() {}

    public static BookingResponse from(
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

    public static Booking toBooking(
            CreateBookingRequest request,
            Hotel hotel,
            RoomType roomType,
            BigDecimal totalPrice
    ) {
        return new Booking(
                request.userId(),
                hotel,
                roomType,
                request.checkIn(),
                request.checkOut(),
                totalPrice
        );
    }
}
