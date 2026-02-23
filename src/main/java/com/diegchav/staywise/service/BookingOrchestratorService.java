package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingOrchestratorService {
    private final BookingService bookingService;
    private final IdempotencyService idempotencyService;

    public BookingOrchestratorService(BookingService bookingService, IdempotencyService idempotencyService) {
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
    }

    public BookingResponse createBooking(
            String idempotencyKey,
            CreateBookingRequest request
    ) {
        try {
            return bookingService.createBooking(idempotencyKey, request);
        } catch (DataIntegrityViolationException ex) {
            return idempotencyService.fetchStoredResponse(idempotencyKey);
        }
    }

    public void cancelBooking(UUID bookingId) {
        bookingService.cancelBooking(bookingId);
    }
}
