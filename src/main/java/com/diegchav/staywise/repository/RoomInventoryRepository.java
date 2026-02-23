package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.RoomInventory;
import com.diegchav.staywise.domain.RoomInventoryId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, RoomInventoryId> {
    boolean existsById(RoomInventoryId id);

    @Modifying
    @Query(value = """
        UPDATE
            room_inventory
        SET
            reserved_rooms = reserved_rooms + 1
        WHERE
            room_type_id = :roomTypeId
            AND
            date = :date
            AND
            total_rooms - reserved_rooms > 0
    """, nativeQuery = true)
    int tryReserve(UUID roomTypeId, LocalDate date);

    @Query("""
        SELECT
            ri.id.roomTypeId,
            MIN(ri.totalRooms - ri.reservedRooms) AS min_available
        FROM
            RoomInventory ri
        WHERE
            ri.hotelId = :hotelId
            AND
            ri.id.date >= :checkIn
            AND
            ri.id.date < :checkOut
        GROUP BY
            ri.id.roomTypeId
        HAVING
            COUNT(DISTINCT ri.id.date) = :days
            AND
            MIN(ri.totalRooms - ri.reservedRooms) > 0
    """)
    List<UUID> findAvailableRoomTypes(
            UUID hotelId,
            LocalDate checkIn,
            LocalDate checkOut,
            int days
    );

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
