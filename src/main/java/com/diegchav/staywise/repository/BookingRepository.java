package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}
