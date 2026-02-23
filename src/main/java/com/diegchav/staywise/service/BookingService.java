package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.domain.Booking;
import com.diegchav.staywise.domain.IdempotencyKey;
import com.diegchav.staywise.exception.SoldOutException;
import com.diegchav.staywise.mapper.BookingMapper;
import com.diegchav.staywise.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class BookingService {
    private final IdempotencyRepository idempotencyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;

    public BookingService(
            IdempotencyRepository idempotencyRepository,
            RoomTypeRepository roomTypeRepository,
            RoomInventoryRepository inventoryRepository,
            BookingRepository bookingRepository
    ) {
        this.idempotencyRepository = idempotencyRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.inventoryRepository = inventoryRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    protected BookingResponse createBooking(
            String idempotencyKey,
            CreateBookingRequest request
    ) {
        var existing = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get().getResponseBody();
        }

        LocalDate date = request.checkIn();
        while (date.isBefore(request.checkOut())) {
            var updated = inventoryRepository.tryReserve(
                    request.roomTypeId(),
                    date
            );

            if (updated == 0) {
                throw new SoldOutException(ErrorMessages.INVENTORY_SOLD_OUT);
            }

            date = date.plusDays(1);
        }

        var booking = persistBooking(request);

        var response = BookingMapper.from(booking, request.hotelId(), request.roomTypeId());

        persistIdempotencyKey(idempotencyKey, response);

        return response;
    }

    private Booking persistBooking(CreateBookingRequest request) {
        // TODO: Add better validation for room type.
        var roomType = roomTypeRepository.findById(request.roomTypeId()).orElseThrow();

        // Calculate total price based on base price.
        var nights = DAYS.between(request.checkIn(), request.checkOut());
        var totalPrice = roomType.getBasePrice().multiply(BigDecimal.valueOf(nights));

        var booking = BookingMapper.toBooking(request, totalPrice);

        return bookingRepository.save(booking);
    }

    private void persistIdempotencyKey(String idempotencyKey, BookingResponse response) {
        var idempotency = new IdempotencyKey(
                idempotencyKey,
                response,
                HttpStatus.OK.value()
        );

        idempotencyRepository.saveAndFlush(idempotency);
    }
}
