package com.diegchav.staywise.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private BigDecimal rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Hotel() {}

    public Hotel(UUID id, String name, String city, String country, BigDecimal rating) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.rating = rating;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }
}
