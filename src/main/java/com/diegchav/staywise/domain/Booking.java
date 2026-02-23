package com.diegchav.staywise.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {}

    public Booking(
            UUID userId,
            UUID roomTypeId,
            UUID hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal totalPrice
    ) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.roomTypeId = roomTypeId;
        this.hotelId = hotelId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = BookingStatus.CONFIRMED;
        this.totalPrice = totalPrice;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomTypeId() {
        return roomTypeId;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            return; // idempotent
        }

        this.status = BookingStatus.CANCELLED;
    }
}
