package com.diegchav.staywise.integration.concurrency;

import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.constant.DockerImages;
import com.diegchav.staywise.domain.entity.Hotel;
import com.diegchav.staywise.domain.entity.RoomType;
import com.diegchav.staywise.repository.BookingRepository;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.BookingOrchestratorService;
import com.diegchav.staywise.service.BookingService;
import com.diegchav.staywise.service.IdempotencyService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({BookingOrchestratorService.class, BookingService.class, IdempotencyService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
class BookingConcurrencyIntegrationTest {
    private static final int INVENTORY_DAYS = 1;
    private static final int INVENTORY_AVAILABLE_ROOMS = 5;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(DockerImages.POSTGRES))
                    .withDatabaseName("staywise");

    @Autowired
    BookingOrchestratorService bookingService;

    @Autowired
    HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    RoomInventoryRepository inventoryRepo;

    @Autowired
    BookingRepository bookingRepo;

    private Hotel hotel;
    private RoomType roomType;

    @BeforeEach
    void setup() {
        hotel = TestDataFactory.createHotel(hotelRepository);
        roomType = TestDataFactory.createRoomType(roomTypeRepository, hotel);

        TestDataFactory.createInventory(
                inventoryRepo,
                roomType,
                LocalDate.now(),
                INVENTORY_DAYS,
                INVENTORY_AVAILABLE_ROOMS,
                0
        );
    }

    @Test
    void shouldNotOverbookUnderConcurrency() throws Exception {
        int threads = 20;

        var executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);

        Runnable task = getBookingTask(ready, start, successCount);

        for (int i = 0; i < threads; i++) {
            executor.submit(task);
        }

        ready.await();
        start.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(INVENTORY_AVAILABLE_ROOMS, successCount.get());
    }

    private Runnable getBookingTask(CountDownLatch ready, CountDownLatch start, AtomicInteger successCount) {
        return () -> {
            ready.countDown();

            try {
                start.await();

                var bookingRequest = new CreateBookingRequest(
                        UUID.randomUUID(),
                        hotel.getId(),
                        roomType.getId(),
                        LocalDate.now().plusDays(0),
                        LocalDate.now().plusDays(1) // book a single day
                );

                bookingService.createBooking(UUID.randomUUID().toString(), bookingRequest);

                successCount.incrementAndGet();
            } catch (Exception ex) {
                // no-op
            }
        };
    }
}
