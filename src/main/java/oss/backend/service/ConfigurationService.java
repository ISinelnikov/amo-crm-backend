package oss.backend.service;

import oss.backend.domain.configuration.ConfigurationSettings;
import oss.backend.util.YamlConfigUtils;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationSettings applicationSettings;

    public ConfigurationService() {
        this.applicationSettings = Objects.requireNonNull(YamlConfigUtils
                        .loadYamlConfig(getConfigPath(), ConfigurationSettings.class),
                "Application settings configuration can't be null."
        );

        if (!this.applicationSettings.isValid()) {
            throw new IllegalStateException("Application settings incorrect.");
        }

        logger.debug("Application configuration: {}.", applicationSettings);
    }

    public ConfigurationSettings getApplicationSettings() {
        return applicationSettings;
    }

    private static String getConfigPath() {
        String configPath = System.getProperty("application.config.path");

        if (!StringUtils.hasText(configPath)) {
            throw new IllegalStateException("Can't start application without config path.");
        }
        return configPath;
    }
}
