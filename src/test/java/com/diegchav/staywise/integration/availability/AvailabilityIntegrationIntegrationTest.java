package com.diegchav.staywise.integration.availability;

import com.diegchav.staywise.domain.entity.Hotel;
import com.diegchav.staywise.domain.entity.RoomType;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.AvailabilityService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class AvailabilityIntegrationIntegrationTest {
    private static final int SEARCH_FOR_DAYS = 3;

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
    AvailabilityService availabilityService;

    private Hotel hotel;
    private RoomType roomType;

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
