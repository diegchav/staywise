package com.diegchav.staywise.api.controller;

import com.diegchav.staywise.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UUID>> search(
            @RequestParam UUID hotelId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {
        var result = service.searchAvailableRoomTypes(
                hotelId, checkIn, checkOut
        );

        return ResponseEntity.ok(result);
    }
}
