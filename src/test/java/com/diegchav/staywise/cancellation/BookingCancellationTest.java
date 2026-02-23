package com.diegchav.staywise.cancellation;

import com.diegchav.staywise.domain.Booking;
import com.diegchav.staywise.domain.BookingStatus;
import com.diegchav.staywise.domain.Hotel;
import com.diegchav.staywise.domain.RoomType;
import com.diegchav.staywise.repository.BookingRepository;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.BookingOrchestratorService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class BookingCancellationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private static final int BOOKED_DAYS = 3;

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
    BookingRepository bookingRepository;

    @Autowired
    BookingOrchestratorService bookingService;

    private Booking booking;

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
        Hotel hotel = TestDataFactory.createHotel(hotelRepository);
        RoomType roomType = TestDataFactory.createRoomType(roomTypeRepository, hotel);

        TestDataFactory.createInventory(
                roomInventoryRepository,
                roomType,
                LocalDate.now(),
                BOOKED_DAYS,
                10,
                1 // make sure there is one reserved room
        );

        var today = LocalDate.now();

        booking = TestDataFactory.createBooking(
                bookingRepository,
                UUID.randomUUID(),
                hotel.getId(),
                roomType.getId(),
                today,
                today.plusDays(BOOKED_DAYS),
                BigDecimal.valueOf(100)
        );
    }

    @AfterEach
    void teardown() {
        bookingRepository.deleteAll();
        roomInventoryRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
    }

    @Test
    void shouldReleaseInventoryWhenBookingCancelled() {
        bookingService.cancelBooking(booking.getId());

        var inventory = roomInventoryRepository.findInventory(
                booking.getHotelId(),
                booking.getRoomTypeId(),
                booking.getCheckIn()
        );

        assertTrue(inventory.isPresent());
        assertEquals(0, inventory.get().getReservedRooms());
    }

    @Test
    void cancelTwiceShouldBeIdempotent() {
        bookingService.cancelBooking(booking.getId());
        bookingService.cancelBooking(booking.getId());

        var updatedBooking = bookingRepository.findById(booking.getId());

        assertTrue(updatedBooking.isPresent());
        assertEquals(BookingStatus.CANCELLED, updatedBooking.get().getStatus());
    }
}
