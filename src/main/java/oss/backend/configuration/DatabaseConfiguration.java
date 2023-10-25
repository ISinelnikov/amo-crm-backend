package oss.backend.configuration;

import oss.backend.domain.configuration.ConfigurationSettings;
import oss.backend.service.ConfigurationService;
import oss.backend.util.DataSourceUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfiguration {
    @Value("${datasource.driverClassName}")
    private String driverClassName;

    @Bean
    public JdbcTemplate getJdbcTemplate(ConfigurationService configurationService) {
        ConfigurationSettings settings = configurationService.getApplicationSettings();
        return DataSourceUtils.getJdbcTemplateByParams(driverClassName, settings.getDatabase().getUrl(),
                settings.getDatabase().getUsername(), settings.getDatabase().getPassword());
    }
}

