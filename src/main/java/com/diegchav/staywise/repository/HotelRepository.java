package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
}
