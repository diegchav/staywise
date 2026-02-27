package com.diegchav.staywise.producer;

import com.diegchav.staywise.domain.event.HotelCreatedEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class HotelEventProducer {
    private final StreamBridge bridge;

    public HotelEventProducer(StreamBridge bridge) {
        this.bridge = bridge;
    }

    public void produceHotelEvents(HotelCreatedEvent event) {
        bridge.send("produceHotelEvents-out-0", event);
    }
}
