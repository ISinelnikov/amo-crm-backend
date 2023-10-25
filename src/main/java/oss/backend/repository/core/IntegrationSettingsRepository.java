package oss.backend.repository.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import oss.backend.domain.IntegrationSettings;
import oss.backend.repository.AbstractRepository;

import java.util.Collection;
import java.util.Collections;

@Repository
public class IntegrationSettingsRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationSettingsRepository.class);

    private static final String SQL_SELECT_INTEGRATION_SETTINGS = """
            select id, integration_name, integration_description, icon_path, initial_api_path, details_api_path, enabled
            from oss_integration_settings
            """;

    public IntegrationSettingsRepository(JdbcTemplate template) {
        super(template);
    }

    public Collection<IntegrationSettings> getAllIntegrationSettings() {
        try {
            return template.query(SQL_SELECT_INTEGRATION_SETTINGS, new MapSqlParameterSource(), getIntegrationSettingsRowMapper());
        } catch (DataAccessException ex) {
            logger.error("Invoke getAllIntegrationSettings() with exception.", ex);
        }
        return Collections.emptyList();
    }

    private static RowMapper<IntegrationSettings> getIntegrationSettingsRowMapper() {
        return (rs, i) -> new IntegrationSettings(
                rs.getString("id"),
                rs.getString("integration_name"),
                rs.getString("integration_description"),
                rs.getString("icon_path"),
                rs.getString("initial_api_path"),
                rs.getString("details_api_path"),
                rs.getBoolean("enabled")
        );
    }
}
