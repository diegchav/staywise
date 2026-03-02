package com.diegchav.staywise.integration.indexing;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.repository.SearchRepository;
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
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class HotelIndexingTest {
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
    private WebTestClient webClient;

    @Autowired
    private SearchRepository searchRepository;

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
