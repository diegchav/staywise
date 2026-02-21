package com.diegchav.staywise.api.controller.internal;

import com.diegchav.staywise.service.InventoryGenerationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/inventory")
public class InventoryAdminController {
    private final InventoryGenerationService service;

    public InventoryAdminController(InventoryGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public void generate(@RequestParam int days) {
        service.generateForNextNthDays(days);
    }
}
