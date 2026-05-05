package com.diegchav.staywise.integration.search;

import com.diegchav.staywise.config.ElasticsearchConfig;
import com.diegchav.staywise.constant.DockerImages;
import com.diegchav.staywise.domain.document.HotelDocument;
import com.diegchav.staywise.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataElasticsearchTest
@Import({
        ElasticsearchConfig.class,
        SearchService.class
})
@Testcontainers
public class SearchBoostingIntegrationTest {
    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchOperations operations;

    @Container
    @ServiceConnection
    static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(DockerImageName.parse(DockerImages.ELASTICSEARCH))
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false");

    @Test
    void shouldBoostExactMatchOnName() {
        var exact = new HotelDocument(
                "1",
                "Hilton",
                "Guadalajara",
                "Mexico",
                4.5,
                Instant.now()
        );

        var partial = new HotelDocument(
                "2",
                "Hilton Reforma",
                "Mexico City",
                "Mexico",
                4.8,
                Instant.now()
        );

        var other = new HotelDocument(
                "3",
                "Grand Hotel",
                "Monterrey",
                "Mexico",
                4.9,
                Instant.now()
        );

        operations.save(exact);
        operations.save(partial);
        operations.save(other);

        operations.indexOps(HotelDocument.class).refresh();

        var result =
                searchService.search(
                        "Hilton",
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10)
                );

        var content = result.getContent();

        assertEquals(2, content.size());

        // Exact match should come first.
        assertEquals("Hilton", content.get(0).name());
        assertEquals("Hilton Reforma", content.get(1).name());
    }

    @Test
    void shouldRankHigherRatingsFirst() {
        var lowRating = new HotelDocument(
                "1",
                "Hilton Guadalajara",
                "Guadalajara",
                "Mexico",
                3.5,
                Instant.now()
        );

        var highRating = new HotelDocument(
                "2",
                "Hilton Guadalajara",
                "Guadalajara",
                "Mexico",
                4.9,
                Instant.now()
        );

        operations.save(lowRating);
        operations.save(highRating);

        operations.indexOps(HotelDocument.class).refresh();

        var result =
                searchService.search(
                        "Hilton",
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10)
                );

        var content = result.getContent();

        assertEquals(2, content.size());

        // Higher rating should come first.
        assertEquals(4.9, content.get(0).rating());
        assertEquals(3.5, content.get(1).rating());
    }

    @Test
    void shouldRespectExplicitSortingOverScore() {
        var lowRating = new HotelDocument(
                "1",
                "Hilton Guadalajara",
                "Guadalajara",
                "Mexico",
                3.5,
                Instant.now()
        );

        var highRating = new HotelDocument(
                "2",
                "Hilton Guadalajara",
                "Guadalajara",
                "Mexico",
                4.9,
                Instant.now()
        );

        operations.save(lowRating);
        operations.save(highRating);

        operations.indexOps(HotelDocument.class).refresh();

        PageRequest pageable =
                PageRequest.of(0, 10, Sort.by("rating").ascending());

        var result =
                searchService.search(
                        "Hilton",
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        var content = result.getContent();

        assertEquals(2, content.size());

        // Sorting should override boosting.
        assertEquals(3.5, content.get(0).rating());
        assertEquals(4.9, content.get(1).rating());
    }
}
