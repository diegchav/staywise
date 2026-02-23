package com.diegchav.staywise.domain;

import com.diegchav.staywise.constant.ErrorMessages;
import com.diegchav.staywise.exception.SoldOutException;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "room_inventory")
public class RoomInventory {
    @EmbeddedId
    private RoomInventoryId id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "reserved_rooms", nullable = false)
    private int reservedRooms;

    protected RoomInventory() {}

    public RoomInventory(
            RoomInventoryId id,
            UUID hotelId,
            int totalRooms
    ) {
        this(id, hotelId, totalRooms, 0);
    }

    public RoomInventory(
            RoomInventoryId id,
            UUID hotelId,
            int totalRooms,
            int reservedRooms
    ) {
        this.id = id;
        this.hotelId = hotelId;
        this.totalRooms = totalRooms;
        this.reservedRooms = reservedRooms;
    }

    public int getAvailableRooms() {
        return totalRooms - reservedRooms;
    }

    public void incrementReservedRooms() {
        if (getAvailableRooms() <= 0) {
            throw new SoldOutException(ErrorMessages.INVENTORY_SOLD_OUT);
        }

        reservedRooms++;
    }
}
