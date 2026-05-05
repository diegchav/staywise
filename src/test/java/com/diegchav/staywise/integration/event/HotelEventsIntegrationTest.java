package com.diegchav.staywise.integration.event;

import com.diegchav.staywise.api.dto.CreateHotelRequest;
import com.diegchav.staywise.domain.event.HotelCreatedEvent;
import com.diegchav.staywise.integration.config.TestContainersConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestContainersConfig.class)
@AutoConfigureMockMvc
public class HotelEventsIntegrationTest {
    private static final String TEST_TOPIC = "hotel.events.test." + UUID.randomUUID();
    private static final String TEST_GROUP = "test-group-" + UUID.randomUUID();

    @Autowired
    private KafkaContainer kafka;

    @Autowired
    private WebTestClient webClient;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.hotels.events.topic", () -> TEST_TOPIC);
    }

    private Consumer<String, HotelCreatedEvent> consumer;

    @BeforeEach
    void setup() {
        var props = new HashMap<String, Object>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, TEST_GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.diegchav.staywise.domain.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.diegchav.staywise.domain.event.HotelCreatedEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singleton(TEST_TOPIC));
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
