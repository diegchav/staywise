package com.diegchav.staywise.integration.event;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.domain.event.HotelCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class HotelEventsIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
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
    private WebTestClient webClient;

    @Value("${app.hotels.events.topic}")
    private String topic;

    private Consumer<String, HotelCreatedEvent> consumer;

    @BeforeEach
    void setup() {
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.diegchav.staywise.domain.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.diegchav.staywise.domain.event.HotelCreatedEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singleton(topic));
    }

    @Test
    void shouldProduceHotelCreatedEvent() {
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
        var records = consumer.poll(Duration.ofSeconds(5));

        assertThat(records.count()).isGreaterThan(0);

        var event = records.iterator().next().value();

        assertNotNull(event.eventId());
        assertNotNull(event.hotelId());
        assertEquals(createRequest.name(), event.name());
        assertEquals(createRequest.address(), event.address());
        assertEquals(createRequest.city(), event.city());
        assertEquals(createRequest.country(), event.country());
        assertNotNull(event.rating());
        assertNotNull(event.occurredAt());
        assertThat(event.version()).isGreaterThan(0);
    }
}
