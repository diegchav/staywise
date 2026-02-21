package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.domain.Booking;
import com.diegchav.staywise.mapper.BookingMapper;
import com.diegchav.staywise.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class BookingService {
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;

    public BookingService(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            RoomInventoryRepository inventoryRepository,
            BookingRepository bookingRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.inventoryRepository = inventoryRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        var booking = persistBooking(request);

        return BookingMapper.from(booking, request.hotelId(), request.roomTypeId());
    }

    private Booking persistBooking(CreateBookingRequest request) {
        var hotel = hotelRepository.findById(request.hotelId()).orElseThrow();
        var roomType = roomTypeRepository.findById(request.roomTypeId()).orElseThrow();

        var inventoryRows = inventoryRepository.lockInventory(
                request.roomTypeId(), request.checkIn(), request.checkOut()
        );

        if (inventoryRows.isEmpty()) {
            throw new IllegalStateException(ErrorMessages.INVENTORY_EMPTY);
        }

        for (var inventory :  inventoryRows) {
            if (inventory.getAvailableRooms() <= 0) {
                throw new IllegalStateException(ErrorMessages.INVENTORY_SOLD_OUT);
            }

            inventory.decrement();
        }

        // Calculate total price based on base price.
        var nights = DAYS.between(request.checkIn(), request.checkOut());
        var totalPrice = roomType.getBasePrice().multiply(BigDecimal.valueOf(nights));

        var booking = BookingMapper.toBooking(request, hotel, roomType, totalPrice);
        return bookingRepository.save(booking);
    }
}
