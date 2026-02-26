package com.diegchav.staywise.integration;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.api.dto.HotelResponse;
import com.diegchav.staywise.api.dto.UpdateHotelRequest;
import com.diegchav.staywise.repository.HotelRepository;
import com.diegchav.staywise.testdata.TestDataFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class HotelIntegrationIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("staywise");

    @Autowired
    private WebTestClient client;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void teardown() {
        hotelRepository.deleteAll();
    }

    @Test
    void shouldCreateHotel() throws JsonProcessingException {
        var createRequest = new CreateHotelRequest(
                "Test hotel",
                "Test address",
                "Test city",
                "Test country"
        );

        client.post()
                .uri("/api/hotels")
                .bodyValue(objectMapper.writeValueAsString(createRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelResponse = response.getResponseBody();

                    assertNotNull(hotelResponse);
                    assertEquals(createRequest.name(), hotelResponse.name());
                    assertEquals(createRequest.address(), hotelResponse.address());
                    assertEquals(createRequest.city(), hotelResponse.city());
                    assertEquals(createRequest.country(), hotelResponse.country());
                    assertNotNull(hotelResponse.rating());
                });
    }

    @Test
    void shouldFailToCreateWithInvalidHotelName() throws JsonProcessingException {
        var createRequest = new CreateHotelRequest(
                "",
                "Test address",
                "Test city",
                "Test country"
        );

        client.post()
                .uri("/api/hotels")
                .bodyValue(objectMapper.writeValueAsString(createRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldGetAllHotels() {
        TestDataFactory.createHotel(hotelRepository);

        client.get()
                .uri("/api/hotels")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelList = response.getResponseBody();
                    assertNotNull(hotelList);
                    assertEquals(1, hotelList.size());
                });
    }

    @Test
    void shouldReturnEmptyListIfThereAreNoHotels() {
        client.get()
                .uri("/api/hotels")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelList = response.getResponseBody();
                    assertNotNull(hotelList);
                    assertEquals(0, hotelList.size());
                });
    }

    @Test
    void shouldGetHotelById() {
        var hotel = TestDataFactory.createHotel(hotelRepository);

        client.get()
                .uri("/api/hotels/{id}", hotel.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelResponse = response.getResponseBody();

                    assertNotNull(hotelResponse);
                    assertEquals(hotel.getName(), hotelResponse.name());
                    assertEquals(hotel.getAddress(), hotelResponse.address());
                    assertEquals(hotel.getCity(), hotelResponse.city());
                    assertEquals(hotel.getCountry(), hotelResponse.country());
                    assertNotNull(hotelResponse.rating());
                });
    }

    @Test
    void shouldReturnNotFoundIfHotelDoesNotExist() {
        var hotelId = UUID.randomUUID();

        client.get()
                .uri("/api/hotels/{id}", hotelId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldUpdateHotel() throws JsonProcessingException {
        var hotel = TestDataFactory.createHotel(hotelRepository);

        var updateRequest = new UpdateHotelRequest(
                "Update name",
                "Update address",
                "Update city",
                "Update country"
        );

        client.patch()
                .uri("/api/hotels/{id}", hotel.getId())
                .bodyValue(objectMapper.writeValueAsString(updateRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelResponse = response.getResponseBody();

                    assertNotNull(hotelResponse);
                    assertEquals(updateRequest.name(), hotelResponse.name());
                    assertEquals(updateRequest.address(), hotelResponse.address());
                    assertEquals(updateRequest.city(), hotelResponse.city());
                    assertEquals(updateRequest.country(), hotelResponse.country());
                    assertNotNull(hotelResponse.rating());
                });
    }

    @Test
    void shouldPartiallyUpdateHotel() throws JsonProcessingException {
        var hotel = TestDataFactory.createHotel(hotelRepository);

        var updateRequest = new UpdateHotelRequest(
                "Update name",
                null,
                null,
                null
        );

        client.patch()
                .uri("/api/hotels/{id}", hotel.getId())
                .bodyValue(objectMapper.writeValueAsString(updateRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody(HotelResponse.class)
                .consumeWith(response -> {
                    var hotelResponse = response.getResponseBody();

                    assertNotNull(hotelResponse);
                    assertEquals(updateRequest.name(), hotelResponse.name());
                    assertEquals(hotel.getAddress(), hotelResponse.address());
                    assertEquals(hotel.getCity(), hotelResponse.city());
                    assertEquals(hotel.getCountry(), hotelResponse.country());
                    assertNotNull(hotelResponse.rating());
                });
    }

    @Test
    void shouldFailUpdateIfEmptyValues() throws JsonProcessingException {
        var hotel = TestDataFactory.createHotel(hotelRepository);

        var updateRequest = new UpdateHotelRequest(
                "",
                "Update address",
                "Update city",
                "Update country"
        );

        client.patch()
                .uri("/api/hotels/{id}", hotel.getId())
                .bodyValue(objectMapper.writeValueAsString(updateRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFailUpdateIfNotFound() throws JsonProcessingException {
        var hotelId = UUID.randomUUID();

        var updateRequest = new UpdateHotelRequest(
                "",
                "Update address",
                "Update city",
                "Update country"
        );

        client.patch()
                .uri("/api/hotels/{id}", hotelId)
                .bodyValue(objectMapper.writeValueAsString(updateRequest))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteHotel() {
        var hotel = TestDataFactory.createHotel(hotelRepository);

        client.delete()
                .uri("/api/hotels/{id}", hotel.getId())
                .exchange()
                .expectStatus().isNoContent();
    }
}
