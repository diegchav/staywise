package com.diegchav.staywise.service;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.api.dto.UpdateHotelRequest;
import com.diegchav.staywise.domain.entity.Hotel;
import com.diegchav.staywise.domain.event.HotelCreatedEvent;
import com.diegchav.staywise.exception.HotelNotFoundException;
import com.diegchav.staywise.exception.HotelUpdateException;
import com.diegchav.staywise.producer.HotelEventProducer;
import com.diegchav.staywise.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {
    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelEventProducer hotelProducer;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel;

    @BeforeEach
    void setup() {
        hotel = new Hotel(
                "Test Name",
                "Test Address",
                "Test City",
                "Test Country",
                BigDecimal.valueOf(5.0)
        );
    }

    @Test
    void shouldCreateHotel() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        doNothing().when(hotelProducer).produceHotelEvents(any(HotelCreatedEvent.class));

        var request = new CreateHotelRequest(
                "Test Name",
                "Test Address",
                "Test City",
                "Test Country"
        );

        var created = hotelService.create(request);

        assertNotNull(created);
    }

    @Test
    void shouldFindAllHotels() {
        when(hotelRepository.findAll()).thenReturn(List.of(hotel));

        assertEquals(1, hotelService.getAll().size());
    }

    @Test
    void shouldFindHotelById() {
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        var foundHotel = hotelService.get(hotel.getId());

        assertNotNull(foundHotel);
    }

    @Test
    void shouldThrowOnCreateIfNotFound() {
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> hotelService.get(hotel.getId()));
    }

    @Test
    void shouldUpdateHotel() {
        var request = new UpdateHotelRequest(
                "Update Name",
                "Update Address",
                "Update City",
                "Update Country"
        );

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        var updated = hotelService.update(hotel.getId(), request);

        assertNotNull(updated);
        assertEquals(request.name(), updated.name());
        assertEquals(request.address(), updated.address());
        assertEquals(request.city(), updated.city());
        assertEquals(request.country(), updated.country());
    }

    @Test
    void shouldIgnoreNullValues() {
        var request = new UpdateHotelRequest(null, null, "Update City", null);

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        var updated = hotelService.update(hotel.getId(), request);

        assertNotNull(updated);
        assertNotNull(updated.name());
        assertNotNull(updated.address());
        assertEquals(request.city(), updated.city());
        assertNotNull(updated.country());
    }

    @Test
    void shouldThrowOnUpdateIfEmptyValues() {
        var request = new UpdateHotelRequest(
                "",
                "Update Address",
                "Update City",
                "Updated Country"
        );

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        assertThrows(HotelUpdateException.class, () -> hotelService.update(hotel.getId(), request));
    }

    @Test
    void shouldThrowOnUpdateIfNotFound() {
        var request = new UpdateHotelRequest(
                "Update Name",
                "Update Address",
                "Update City",
                "Update Country"
        );

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> hotelService.update(hotel.getId(), request));
    }

    @Test
    void shouldDeleteHotel() {
        doNothing().when(hotelRepository).deleteById(hotel.getId());

        hotelService.deleteById(hotel.getId());
    }
}
