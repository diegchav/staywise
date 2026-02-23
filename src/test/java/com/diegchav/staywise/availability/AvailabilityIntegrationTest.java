package com.diegchav.staywise.availability;

import com.diegchav.staywise.domain.Hotel;
import com.diegchav.staywise.domain.RoomType;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.AvailabilityService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AvailabilityIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private static final int SEARCH_FOR_DAYS = 3;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Autowired
    RoomInventoryRepository roomInventoryRepository;

    @Autowired
    AvailabilityService availabilityService;

    private Hotel hotel;
    private RoomType roomType;

    @BeforeAll
    static void init() {
        postgres.start();
    }

    @AfterAll
    static void done() {
        postgres.stop();
    }

    @BeforeEach
    void setup() {
        hotel = TestDataFactory.createHotel(hotelRepository);
        roomType = TestDataFactory.createRoomType(roomTypeRepository, hotel);
    }

    @AfterEach
    void teardown() {
        roomInventoryRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldReturnRoomTypesWhenAvailableForAllDays() {
        LocalDate today = LocalDate.now();

        // Create inventory for 3 days.
        TestDataFactory.createInventory(
                roomInventoryRepository,
                roomType,
                today,
                SEARCH_FOR_DAYS,
                5,
                0
        );

        var result = availabilityService.searchAvailableRoomTypes(
                hotel.getId(),
                today,
                today.plusDays(SEARCH_FOR_DAYS)
        );

        assertTrue(result.contains(roomType.getId()));
    }

    @Test
    @Transactional
    void shouldNotReturnRoomTypesWhenNotAvailableForAllDays() {
        LocalDate today = LocalDate.now();

        TestDataFactory.createInventory(
                roomInventoryRepository,
                roomType,
                today,
                SEARCH_FOR_DAYS,
                0,
                5
        );

        var result = availabilityService.searchAvailableRoomTypes(
                hotel.getId(),
                today,
                today.plusDays(SEARCH_FOR_DAYS)
        );

        assertEquals(0, result.size());
    }
}
