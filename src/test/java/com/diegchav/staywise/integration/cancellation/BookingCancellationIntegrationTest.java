package com.diegchav.staywise.integration.cancellation;

import com.diegchav.staywise.domain.entity.Booking;
import com.diegchav.staywise.domain.entity.BookingStatus;
import com.diegchav.staywise.domain.entity.Hotel;
import com.diegchav.staywise.domain.entity.RoomType;
import com.diegchav.staywise.repository.BookingRepository;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.BookingOrchestratorService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class BookingCancellationIntegrationTest {
    private static final int BOOKED_DAYS = 3;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("staywise");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new  KafkaContainer(DockerImageName.parse("apache/kafka:3.9.2"));

    @Container
    @ServiceConnection
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch:8.19.12"))
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false");

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
