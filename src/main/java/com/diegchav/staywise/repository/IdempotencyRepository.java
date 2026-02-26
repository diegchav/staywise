package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, UUID> {
    Optional<IdempotencyKey> findByIdempotencyKey(String key);
}
