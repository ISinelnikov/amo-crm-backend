package oss.backend.controller.integration;

import oss.backend.domain.IntegrationSettings;
import oss.backend.service.IntegrationSettingsService;

import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration-settings")
public class IntegrationSettingsController {
    private final IntegrationSettingsService integrationSettingsService;

    public IntegrationSettingsController(IntegrationSettingsService integrationSettingsService) {
        this.integrationSettingsService = integrationSettingsService;
    }

    @GetMapping
    public ResponseEntity<Collection<IntegrationSettings>> getAllIntegrationSettings() {
        return ResponseEntity.ok(integrationSettingsService.getAllIntegrationSettings());
    }
}
