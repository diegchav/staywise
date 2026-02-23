package com.diegchav.staywise.service;

import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
    private final RoomInventoryRepository inventoryRepository;

    public AvailabilityService(RoomInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<UUID> searchAvailableRoomTypes(
            UUID hotelId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        var days = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        if (days <= 0) {
            throw new IllegalArgumentException(ErrorMessages.AVAILABILITY_INVALID_DATE_RANGE);
        }

        return inventoryRepository.findAvailableRoomTypes(hotelId, checkIn, checkOut, days);
    }
}
