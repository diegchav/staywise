package com.diegchav.staywise.service;

import com.diegchav.staywise.domain.RoomInventory;
import com.diegchav.staywise.domain.RoomInventoryId;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class InventoryGenerationService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository roomInventoryRepository;

    public InventoryGenerationService(RoomTypeRepository roomTypeRepository, RoomInventoryRepository roomInventoryRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomInventoryRepository = roomInventoryRepository;
    }

    @Transactional
    public void generateForNextNthDays(int days) {
        var today = LocalDate.now();
        var endDate = today.plusDays(days);

        var roomTypes = roomTypeRepository.findAll();
        var toSave = new ArrayList<RoomInventory>();

        for (var roomType : roomTypes) {
            for (var date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
                var id = new RoomInventoryId(roomType.getId(), date);

                if (!roomInventoryRepository.existsById(id)) {
                    var inventory = new RoomInventory(
                            id,
                            roomType.getTotalRooms()
                    );

                    toSave.add(inventory);
                }
            }
        }

        roomInventoryRepository.saveAll(toSave);
    }
}
