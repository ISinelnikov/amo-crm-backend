package oss.backend.repository.core;

import oss.backend.domain.AmoCrmCredentials;
import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;
import oss.backend.util.RepositoryUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class IntegrationRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationRepository.class);

    private static final String SQL_SELECT_INTEGRATION_INFO_BY_CLIENT_ID = """
            select client_id,
                   client_secret,
                   access_token,
                   refresh_token,
                   last_refresh_token_date_update,
                   date_create
            from integration_info
            where client_id = :client_id
            """;

    private static final String SQL_INSERT_INTEGRATION_INFO = """
            insert into integration_info(client_id, client_secret, access_token,
                refresh_token, last_refresh_token_date_update, date_create)
            values (:client_id, :client_secret, :access_token,
                :refresh_token, :last_refresh_token_date_update, :date_create)
            """;


    private static final String SQL_UPDATE_INTEGRATION_INFO = """
            update integration_info
            set access_token = :access_token, refresh_token = :refresh_token, last_refresh_token_date_update = :last_refresh_token_date_update
            where client_id = :client_id
            """;

    public IntegrationRepository(JdbcTemplate template) {
        super(template);
    }

    @Nullable
    public AmoCrmCredentials getIntegrationCredentialsById(String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource("client_id", clientId);
        try {
            return template.queryForObject(SQL_SELECT_INTEGRATION_INFO_BY_CLIENT_ID, params,
                    (rs, i) -> new AmoCrmCredentials(
                            rs.getString("client_id"),
                            rs.getString("client_secret"),
                            rs.getString("access_token"),
                            rs.getString("refresh_token"),
                            RepositoryUtils.extractDateFromRS(rs, "last_refresh_token_date_update"),
                            RepositoryUtils.extractDateFromRS(rs, "date_create")
                    )
            );
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getIntegrationInfoByClientId({}) with exception.", clientId);
        }
        return null;
    }

    public boolean addIntegration(String clientId, String clientSecret, String accessToken, String refreshToken) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("client_secret", clientSecret);
        params.addValue("access_token", accessToken);
        params.addValue("refresh_token", refreshToken);
        LocalDateTime now = DateUtils.now();
        params.addValue("last_refresh_token_date_update", Timestamp.valueOf(now));
        params.addValue("date_create", Timestamp.valueOf(now));

        try {
            return template.update(SQL_INSERT_INTEGRATION_INFO, params) > 0;
        } catch (DataAccessException ex) {
            logger.error("Invoke addIntegration({}, {}, {}, {}) with exception.",
                    clientId, clientSecret, accessToken, refreshToken, ex);
        }
        return false;
    }

    public void refreshIntegration(String clientId, String accessToken, String refreshToken) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("access_token", accessToken);
        params.addValue("refresh_token", refreshToken);
        params.addValue("last_refresh_token_date_update", Timestamp.valueOf(DateUtils.now()));

        try {
            template.update(SQL_UPDATE_INTEGRATION_INFO, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke refreshIntegration({}, {}, {}) with exception.", clientId, accessToken, refreshToken, ex);
        }
    }
}
