package com.diegchav.staywise.integration.search;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.domain.document.HotelDocument;
import com.diegchav.staywise.integration.config.TestContainersConfig;
import com.diegchav.staywise.repository.SearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
public class SearchIntegrationTest {
    private static final String TEST_INDEX = "hotels-test-" + UUID.randomUUID();
    private static final String TEST_GROUP = "test-group-" + UUID.randomUUID();

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.hotels.search.index", () -> TEST_INDEX);
        registry.add("app.hotels.events.group", () -> TEST_GROUP);
    }

    @BeforeEach
    void setup() {
        IndexOperations indexOps = operations.indexOps(HotelDocument.class);

        if (indexOps.exists()) {
            indexOps.delete();
        }

        indexOps.create();
        indexOps.putMapping();
    }

    @Test
    void shouldSearchByCity() {
        createHotel("Hotel 1", "CDMX", "Mexico");
        createHotel("Hotel 1", "Monterrey", "Mexico");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertEquals(2, searchRepository.count());
                });

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
    void shouldSearchByCityAndCountry() {
        createHotel("Hotel 1", "CDMX", "Mexico");
        createHotel("Hotel 2", "New York", "USA");
        createHotel("Hotel 3", "Madrid", "Spain");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertEquals(3, searchRepository.count())
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
                .untilAsserted(() -> {
                    assertEquals(2, searchRepository.count());
                });

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
