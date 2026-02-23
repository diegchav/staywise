package com.diegchav.staywise.concurrency;

import com.diegchav.staywise.api.dto.CreateBookingRequest;
import com.diegchav.staywise.domain.Hotel;
import com.diegchav.staywise.domain.RoomType;
import com.diegchav.staywise.repository.BookingRepository;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.repository.RoomInventoryRepository;
import com.diegchav.staywise.repository.RoomTypeRepository;
import com.diegchav.staywise.service.BookingOrchestratorService;
import com.diegchav.staywise.testdata.TestDataFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookingConcurrencyTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    private static final int INVENTORY_DAYS = 1;
    private static final int INVENTORY_AVAILABLE_ROOMS = 5;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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

    Hotel hotel;
    RoomType roomType;

    @BeforeAll
    static void init() {
        postgres.start();
    }

    @AfterAll
    static void done() {
        postgres.stop();
    }

    @BeforeEach
    void setupInventory() {
        bookingRepo.deleteAll();
        inventoryRepo.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();

        hotel = TestDataFactory.createHotel(hotelRepository);
        roomType = TestDataFactory.createRoomType(roomTypeRepository, hotel);

        TestDataFactory.createInventory(
                roomType,
                inventoryRepo,
                LocalDate.now(),
                INVENTORY_DAYS,
                INVENTORY_AVAILABLE_ROOMS
        );
    }

    @Test
    void shouldNotOverbookUnderConcurrency() throws Exception {
        int threads = 20;

        var executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        Runnable task = getBookingTask(ready, start);

        for (int i = 0; i < threads; i++) {
            executor.submit(task);
        }

        ready.await();
        start.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long totalBookings = bookingRepo.count();

        assertEquals(INVENTORY_AVAILABLE_ROOMS, totalBookings);
    }

    private Runnable getBookingTask(CountDownLatch ready, CountDownLatch start) {
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

            } catch (Exception ex) {
                // no-op
            }
        };
    }
}
