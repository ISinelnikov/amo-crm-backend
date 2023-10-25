package oss.newamo.repository.impl;

import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;
import oss.newamo.domain.credentials.ClientCredentials;
import oss.newamo.domain.credentials.InitialCredentials;
import oss.newamo.repository.ClientConnectionRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class PgClientConnectionRepository extends AbstractRepository implements ClientConnectionRepository {
    private static final Logger logger = LoggerFactory.getLogger(PgClientConnectionRepository.class);

    private static final String SQL_SELECT_IS_EXIST_CLIENT_CONNECTION = """
            select exists(select 1 from oss_amo_client_credentials where space_id = :space_id) as is_exist
            """;

    private static final String SQL_INSERT_CLIENT_CONNECTION = """
            insert into oss_amo_client_credentials (space_id, operation_id, redirect_uri, creation_date)
            values (:space_id, :operation_id, :redirect_uri, :creation_date)
            """;

    private static final String SQL_UPDATE_CLIENT_CONNECTION_SECRET = """
            update oss_amo_client_credentials
            set client_id = :client_id, client_secret = :client_secret
            where operation_id = :operation_id
            """;

    private static final String SQL_UPDATE_CLIENT_CONNECTION_CODE = """
            update oss_amo_client_credentials
            set client_id = :client_id, code = :code, referer = :referer
            where operation_id = :operation_id
            """;

    private static final String SQL_SELECT_INITIAL_CREDENTIALS = """
            select space_id, client_id, client_secret, code, redirect_uri, referer
            from oss_amo_client_credentials
            where client_id = :client_id
              and client_secret is not null
              and code is not null
              and referer is not null
            """;

    private static final String SQL_SELECT_CLIENT_CREDENTIALS = """
            select space_id, client_id, access_token, refresh_token, redirect_uri, referer
            from oss_amo_client_credentials
            where client_id = :client_id
              and client_secret is not null
              and code is not null
              and referer is not null
            """;

    private static final String SQL_SELECT_ALL_REFRESHABLE_CLIENT_CREDENTIALS = """
            select space_id, client_id, access_token, refresh_token, redirect_uri, referer
            from oss_amo_client_credentials
            where last_refresh_date is not null
              and last_refresh_date < :refresh_time
            """;

    private static final String SQL_UPDATE_CLIENT_TOKENS = """
            update oss_amo_client_credentials
            set access_token = :access_token, refresh_token = :refresh_token, last_refresh_date = :last_refresh_date
            where client_id = :client_id
            """;

    public PgClientConnectionRepository(JdbcTemplate template) {
        super(template);
    }

    @Override
    public boolean isExistClientConnection(long spaceId) {
        MapSqlParameterSource params = new MapSqlParameterSource("space_id", spaceId);

        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_CLIENT_CONNECTION, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistClientConnection({}) with exception.", spaceId, ex);
        }

        return false;
    }

    @Override
    public void initialClientConnection(String operationId, long spaceId, String redirectUri) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("space_id", spaceId);
        params.addValue("operation_id", operationId);
        params.addValue("redirect_uri", redirectUri);
        params.addValue("creation_date", Timestamp.valueOf(DateUtils.now()));

        try {
            template.update(SQL_INSERT_CLIENT_CONNECTION, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke initialClientConnection({}, {}, {}) with exception.",
                    operationId, spaceId, redirectUri, ex);
        }
    }

    @Override
    public void updateClientConnectionSecret(String operationId, String clientId, String clientSecret) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("operation_id", operationId);
        params.addValue("client_id", clientId);
        params.addValue("client_secret", clientSecret);

        try {
            template.update(SQL_UPDATE_CLIENT_CONNECTION_SECRET, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateClientConnectionSecret({}, {}, {}) with exception.",
                    operationId, clientId, clientSecret, ex);
        }
    }

    @Override
    public void updateClientConnectionCode(String operationId, String code, String referer, String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("operation_id", operationId);
        params.addValue("client_id", clientId);
        params.addValue("code", code);
        params.addValue("referer", referer);

        try {
            template.update(SQL_UPDATE_CLIENT_CONNECTION_CODE, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateClientConnectionCode({}, {}, {}, {}) with exception.",
                    operationId, code, referer, clientId, ex);
        }
    }

    @Nullable
    @Override
    public InitialCredentials getInitialCredentials(String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource("client_id", clientId);
        try {
            return template.queryForObject(SQL_SELECT_INITIAL_CREDENTIALS, params, (rs, i) -> new InitialCredentials(
                    rs.getLong("space_id"),
                    rs.getString("client_id"),
                    rs.getString("redirect_uri"),
                    rs.getString("referer"),
                    rs.getString("client_secret"),
                    rs.getString("code")
            ));
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getInitialCredentials({}) with exception.", clientId, ex);
        }
        return null;
    }

    @Nullable
    @Override
    public ClientCredentials getClientCredentials(String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource("client_id", clientId);
        try {
            return template.queryForObject(SQL_SELECT_CLIENT_CREDENTIALS, params, getClientCredentialsRowMapper());
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getClientCredentials({}) with exception.", clientId, ex);
        }
        return null;
    }

    @Override
    public void refreshTokens(String clientId, String accessToken, String refreshToken) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("access_token", accessToken);
        params.addValue("refresh_token", refreshToken);
        params.addValue("last_refresh_date", Timestamp.valueOf(DateUtils.now()));

        try {
            template.update(SQL_UPDATE_CLIENT_TOKENS, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke refreshTokens({}, {}, {}) with exception.", clientId, accessToken, refreshToken, ex);
        }
    }

    @Override
    public Collection<ClientCredentials> getAllRefreshableClientCredentials(long hours) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("refresh_time", Timestamp.valueOf(DateUtils.now().minusHours(hours)));
        try {
            return template.query(SQL_SELECT_ALL_REFRESHABLE_CLIENT_CREDENTIALS, params, getClientCredentialsRowMapper());
        } catch (DataAccessException ex) {
            logger.error("Invoke getAllRefreshableClientCredentials({}) with exception.", hours, ex);
        }
        return Collections.emptyList();
    }

    private static RowMapper<ClientCredentials> getClientCredentialsRowMapper() {
        return (rs, i) -> new ClientCredentials(
                rs.getLong("space_id"),
                rs.getString("client_id"),
                rs.getString("redirect_uri"),
                rs.getString("referer"),
                rs.getString("access_token"),
                rs.getString("refresh_token")
        );
    }
}
