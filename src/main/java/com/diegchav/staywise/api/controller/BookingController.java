package com.diegchav.staywise.api.controller;

import com.diegchav.staywise.api.dto.BookingResponse;
import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.constant.CustomHttpHeaders;
import com.diegchav.staywise.service.BookingOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingOrchestratorService bookingService;

    public BookingController(BookingOrchestratorService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @RequestHeader(CustomHttpHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.ok(bookingService.createBooking(idempotencyKey, request));
    }
}
