package com.diegchav.staywise.integration.search;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.repository.SearchRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class SearchIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
            .withDatabaseName("staywise");

    @Container
    @ServiceConnection
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.2"));

    @Container
    @ServiceConnection
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch:8.19.12"))
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false");

    @Autowired
    private SearchRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    @AfterEach
    void teardown() {
        repository.deleteAll();
    }

    @Test
    void shouldSearchByCity() {
        createHotel("Hotel 1", "CDMX", "Mexico");
        createHotel("Hotel 1", "Monterrey", "Mexico");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertEquals(2, repository.count()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("city", "CDMX")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].city").isEqualTo("CDMX");
    }

    @Test
    void shouldSearchByCityAndCountryAndRating() {
        createHotel("Hotel 1", "CDMX", "Mexico");
        createHotel("Hotel 2", "New York", "USA");
        createHotel("Hotel 3", "Madrid", "Spain");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertEquals(3, repository.count())
                );

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("city", "CDMX")
                        .queryParam("country", "Mexico")
                        .queryParam("minRating", 4.0)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].name").isEqualTo("Hotel 1");
    }

    @Test
    void shouldSortByNameAscending() {
        createHotel("Zeta Hotel", "CDMX", "Mexico");
        createHotel("Alpha Hotel", "CDMX", "Mexico");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertEquals(2, repository.count()));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("sort", "name,asc")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].name").isEqualTo("Alpha Hotel")
                .jsonPath("$.content[1].name").isEqualTo("Zeta Hotel");
    }

    private void createHotel(String name, String city, String country) {
        var createRequest = new CreateHotelRequest(
                name,
                "Test Address",
                city,
                country
        );

        webTestClient.post()
                .uri("/api/hotels")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();
    }
}
