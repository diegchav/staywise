package com.diegchav.staywise.consumer;

import com.diegchav.staywise.domain.document.HotelDocument;
import com.diegchav.staywise.domain.event.HotelCreatedEvent;
import com.diegchav.staywise.repository.SearchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class HotelEventConsumer {
    @Bean
    public Consumer<HotelCreatedEvent> consumeHotelEvents(SearchRepository searchRepository) {
        return event -> {
            var doc = new HotelDocument(
                    event.hotelId().toString(),
                    event.name(),
                    event.city(),
                    event.country(),
                    event.rating().doubleValue(),
                    event.occurredAt()
            );

            searchRepository.save(doc);
        };
    }
}
