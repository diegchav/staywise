package com.diegchav.staywise.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RoomType() {}

    public RoomType(UUID id, UUID hotelId, String name, int capacity, int totalRooms, BigDecimal basePrice) {
        this.id = id;
        this.hotelId = hotelId;
        this.name = name;
        this.capacity = capacity;
        this.totalRooms = totalRooms;
        this.basePrice = basePrice;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}
