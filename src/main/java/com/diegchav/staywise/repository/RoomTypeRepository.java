package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> {
}
