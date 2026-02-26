package com.diegchav.staywise.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RoomInventoryId {
    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(nullable = false)
    private LocalDate date;

    protected RoomInventoryId() {}

    public RoomInventoryId(UUID roomTypeId, LocalDate date) {
        this.roomTypeId = roomTypeId;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RoomInventoryId that = (RoomInventoryId) o;
        return Objects.equals(roomTypeId, that.roomTypeId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTypeId, date);
    }
}
