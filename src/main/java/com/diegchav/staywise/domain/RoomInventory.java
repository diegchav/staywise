package com.diegchav.staywise.domain;

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

    @Column(name = "available_rooms", nullable = false)
    private int availableRooms;

    protected RoomInventory() {}

    public RoomInventory(
            RoomType roomType,
            RoomInventoryId id,
            int availableRooms
    ) {
        this.roomType = roomType;
        this.id = id;
        this.availableRooms = availableRooms;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void decrement() {
        if (availableRooms <= 0) {
            throw new IllegalStateException("No availability");
        }

        availableRooms--;
    }
}
