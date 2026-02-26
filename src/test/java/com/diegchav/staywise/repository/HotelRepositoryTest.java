package com.diegchav.staywise.repository;

import com.diegchav.staywise.base.BaseTest;
import com.diegchav.staywise.domain.Hotel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HotelRepositoryTest extends BaseTest {
    @Autowired
    private HotelRepository hotelRepository;

    private Hotel hotel;

    @BeforeEach
    void setup() {
        hotel = new Hotel(
                "Test Hotel",
                "Test Address",
                "Test City",
                "Test Country",
                BigDecimal.valueOf(5.0)
        );
    }

    @Test
    void shouldSaveHotel() {
        var savedHotel = hotelRepository.save(hotel);

        assertNotNull(hotelRepository.findById(savedHotel.getId()));
    }

    @Test
    void shouldFindAllHotels() {
        hotelRepository.save(hotel);

        var hotels = hotelRepository.findAll();

        assertEquals(1, hotels.size());
    }

    @Test
    void shouldFindHotelById() {
        hotelRepository.save(hotel);

        var foundHotel = hotelRepository.findById(hotel.getId());

        assertTrue(foundHotel.isPresent());
        assertEquals(hotel.getId(), foundHotel.get().getId());
    }

    @Test
    void shouldFailIfNameIsNull() {
        hotel = new Hotel(
                null,
                "Test Address",
                "Test City",
                "Test Country",
                BigDecimal.valueOf(5.0)
        );

        assertThrows(DataIntegrityViolationException.class,
                () -> hotelRepository.saveAndFlush(hotel));
    }

    @Test
    void shouldFailIfAddressIsNull() {
        hotel = new Hotel(
                "Test Name",
                null,
                "Test City",
                "Test Country",
                BigDecimal.valueOf(5.0)
        );

        assertThrows(DataIntegrityViolationException.class,
                () -> hotelRepository.saveAndFlush(hotel));
    }

    @Test
    void shouldFailIfCityIsNull() {
        hotel = new Hotel(
                "Test Name",
                "Test Address",
                null,
                "Test Country",
                BigDecimal.valueOf(5.0)
        );

        assertThrows(DataIntegrityViolationException.class,
                () -> hotelRepository.saveAndFlush(hotel));
    }

    @Test
    void shouldFailIfCountryIsNull() {
        hotel = new Hotel(
                "Test Name",
                "Test Address",
                "Test City",
                null,
                BigDecimal.valueOf(5.0)
        );

        assertThrows(DataIntegrityViolationException.class,
                () -> hotelRepository.saveAndFlush(hotel));
    }

    @Test
    void shouldDeleteHotel() {
        hotelRepository.save(hotel);

        hotelRepository.deleteById(hotel.getId());

        assertTrue(hotelRepository.findById(hotel.getId()).isEmpty());
    }
}
