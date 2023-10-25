package oss.backend.service;

import oss.backend.domain.IntegrationSettings;
import oss.backend.repository.core.IntegrationSettingsRepository;

import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class IntegrationSettingsService {
    private final IntegrationSettingsRepository integrationSettingsRepository;

    public IntegrationSettingsService(IntegrationSettingsRepository integrationSettingsRepository) {
        this.integrationSettingsRepository = integrationSettingsRepository;
    }

    public Collection<IntegrationSettings> getAllIntegrationSettings() {
        return integrationSettingsRepository.getAllIntegrationSettings();
    }
}
