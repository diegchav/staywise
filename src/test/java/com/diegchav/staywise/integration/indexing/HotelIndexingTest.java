package com.diegchav.staywise.integration.indexing;

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
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
public class HotelIndexingTest {
    private static final String TEST_INDEX = "hotels-test-" + UUID.randomUUID();
    private static final String TEST_GROUP = "test-group-" + UUID.randomUUID();

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private WebTestClient webClient;

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
    void shouldConsumeEventAndIndexDocument() {
        // Arrange
        var createRequest = new CreateHotelRequest(
                "Test name",
                "Test address",
                "Test city",
                "Test country"
        );

        // Act
        webClient.post()
                .uri("/api/hotels")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        // Assert
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var docs = StreamSupport
                            .stream(searchRepository.findAll().spliterator(), false)
                            .toList();

                    assertEquals(1, docs.size());

                    var doc = docs.getFirst();
                    assertEquals(createRequest.name(), doc.getName());
                    assertEquals(createRequest.city(), doc.getCity());
                    assertEquals(createRequest.country(), doc.getCountry());
                    assertEquals(5.0, doc.getRating());
                });
    }
}
