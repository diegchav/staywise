package com.diegchav.staywise.integration.config;

import com.diegchav.staywise.constant.DockerImages;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(DockerImages.POSTGRES))
                    .withDatabaseName("staywise");

    @Container
    static final KafkaContainer kafka =
            new  KafkaContainer(DockerImageName.parse(DockerImages.KAFKA));

    @Container
    static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse(DockerImages.ELASTICSEARCH))
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false");

    static {
        postgres.start();
        kafka.start();
        elasticsearch.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgres;
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return kafka;
    }

    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer() {
        return elasticsearch;
    }
}
