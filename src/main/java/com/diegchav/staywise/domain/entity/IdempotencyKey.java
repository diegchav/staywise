package com.diegchav.staywise.domain.entity;

import com.diegchav.staywise.api.dto.BookingResponse;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private BookingResponse responseBody;

    @Column(name = "status_code")
    private int statusCode;

    private Instant createdAt;

    protected IdempotencyKey() {}

    public IdempotencyKey(String idempotencyKey, BookingResponse responseBody) {
        this.idempotencyKey = idempotencyKey;
        this.responseBody = responseBody;
        this.createdAt = Instant.now();
    }

    public IdempotencyKey(String idempotencyKey, BookingResponse responseBody, int statusCode) {
        this.idempotencyKey = idempotencyKey;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.createdAt = Instant.now();
    }

    public BookingResponse getResponseBody() {
        return responseBody;
    }
}
