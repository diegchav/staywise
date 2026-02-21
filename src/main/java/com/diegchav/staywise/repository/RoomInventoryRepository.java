package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.RoomInventory;
import com.diegchav.staywise.domain.RoomInventoryId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, RoomInventoryId> {
    boolean existsById(RoomInventoryId id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT
            ri
        FROM
            RoomInventory ri
        WHERE
            ri.id.roomTypeId = :roomTypeId
            AND
            ri.id.date >= :startDate
            AND
            ri.id.date < :endDate
    """)
    List<RoomInventory> lockInventory(UUID roomTypeId, LocalDate startDate, LocalDate endDate);
}
