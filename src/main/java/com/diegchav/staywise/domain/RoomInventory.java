package com.diegchav.staywise.domain;

import com.diegchav.staywise.constant.ErrorMessages;
import jakarta.persistence.*;

@Entity
@Table(name = "room_inventory")
public class RoomInventory {
    @EmbeddedId
    private RoomInventoryId id;

    @MapsId("roomTypeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "reserved_rooms", nullable = false)
    private int reservedRooms;

    protected RoomInventory() {}

    public RoomInventory(
            RoomType roomType,
            RoomInventoryId id,
            int totalRooms
    ) {
        this.roomType = roomType;
        this.id = id;
        this.totalRooms = totalRooms;
        this.reservedRooms = 0;
    }

    public int getAvailableRooms() {
        return totalRooms - reservedRooms;
    }

    public void incrementReservedRooms() {
        if (getAvailableRooms() <= 0) {
            throw new IllegalStateException(ErrorMessages.INVENTORY_EMPTY);
        }

        reservedRooms++;
    }
}
