package com.diegchav.staywise.testdata;

import com.diegchav.staywise.domain.Hotel;
import com.diegchav.staywise.domain.RoomInventory;
import com.diegchav.staywise.domain.RoomInventoryId;
import com.diegchav.staywise.domain.RoomType;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TestDataFactory {
    private TestDataFactory() {}

    public static Hotel createHotel(HotelRepository hotelRepository) {
        var hotel = new Hotel(
                UUID.randomUUID(),
                "Test hotel",
                "Test city",
                "Test country",
                BigDecimal.valueOf(2.5)
        );

        return hotelRepository.save(hotel);
    }

    public static RoomType createRoomType(
            RoomTypeRepository roomTypeRepository,
            Hotel hotel
            ) {
        var roomType = new RoomType(
                UUID.randomUUID(),
                hotel.getId(),
                "Test room type",
                2,
                10,
                BigDecimal.valueOf(100)
        );

        return roomTypeRepository.save(roomType);
    }

    public static void createInventory(
            RoomType roomType,
            RoomInventoryRepository inventoryRepository,
            LocalDate startDate,
            int days,
            int availableRooms
    ) {
        for (int i = 0; i < days; i++) {
            var date = startDate.plusDays(i);
            var inventoryId = new RoomInventoryId(roomType.getId(), date);
            var roomInventory = new RoomInventory(
                    inventoryId,
                    roomType.getHotelId(),
                    availableRooms
            );

            inventoryRepository.save(roomInventory);
        }
    }
}
