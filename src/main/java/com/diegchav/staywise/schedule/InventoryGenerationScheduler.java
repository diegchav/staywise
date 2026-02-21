package com.diegchav.staywise.schedule;

import com.diegchav.staywise.service.InventoryGenerationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryGenerationScheduler {
    private final InventoryGenerationService service;

    public InventoryGenerationScheduler(InventoryGenerationService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 2 * * *") // daily at 2am
    public void runDaily() {
        service.generateForNextNthDays(90); // next 3 months
    }
}
