package oss.backend.repository.core;

import oss.backend.repository.AbstractRepository;
import oss.backend.security.AuthenticationRequest;
import oss.backend.security.TwoFactorAuthType;
import oss.backend.util.DateUtils;
import oss.backend.util.MappingUtils;
import oss.backend.util.RepositoryUtils;

import java.sql.Timestamp;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import com.warrenstrange.googleauth.ICredentialRepository;

@Repository
public class SecurityRepository extends AbstractRepository implements ICredentialRepository {
    private static final Logger logger = LoggerFactory.getLogger(SecurityRepository.class);

    private static final String SQL_INSERT_GA_PROFILE_CREDENTIALS = """
            insert into google_authenticator_profile_credentials
                (profile_id, ga_secret_key, validation_code, scratch_codes, date_create)
            values ((select id from profile where username = :username),
                    :ga_secret_key, :validation_code, :scratch_codes, :date_create)
            """;

    private static final String SQL_SELECT_GA_SECRET_KEY = """
            select ga_secret_key
            from google_authenticator_profile_credentials
            where profile_id = (select id from profile where username = :username)
            """;

    private static final String SQL_DELETE_GA_PROFILE_CREDENTIALS = """
            delete
            from google_authenticator_profile_credentials
            where profile_id = (select id from profile where username = :username)
            """;

    private static final String SQL_INSERT_ONE_FACTOR_AUTHENTICATION_REQUEST = """
            insert into authentication_request (request_id, date_create, profile_id, auth_type, completed)
            values (:request_id, :date_create, :profile_id, :auth_type, true)
            """;

    private static final String SQL_INSERT_TWO_FACTOR_AUTHENTICATION_REQUEST = """
            insert into authentication_request (request_id, date_create, profile_id, auth_type, two_factor_code)
            values (:request_id, :date_create, :profile_id, :auth_type, :two_factor_code)
            """;

    private static final String SQL_SELECT_AUTHENTICATION_REQUEST_BY_ID = """
            select request_id,
                   authentication_request.date_create,
                   profile_id,
                   username,
                   password,
                   authentication_request.auth_type,
                   two_factor_code
            from authentication_request,
                 profile
            where authentication_request.profile_id = profile.id
              and request_id = :request_id
              and completed = false
            """;

    private static final String SQL_UPDATE_AUTHENTICATION_REQUEST = """
            update authentication_request
            set completed = true
            where request_id = :request_id
            """;

    public SecurityRepository(JdbcTemplate template) {
        super(template);
    }

    @Nullable
    @Override
    public String getSecretKey(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource("username", username);
        try {
            return template.queryForObject(SQL_SELECT_GA_SECRET_KEY, params, (rs, i) -> rs.getString("ga_secret_key"));
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getSecretKey({}) with exception.", username, ex);
        }
        return null;
    }

    @Override
    public void saveUserCredentials(String username, String secretKey, int validationCode, List<Integer> scratchCodes) {
        deleteUserCredentials(username);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", username);
        params.addValue("ga_secret_key", secretKey);
        params.addValue("validation_code", validationCode);
        params.addValue("scratch_codes", MappingUtils.convertObjectToJson(scratchCodes));
        params.addValue("date_create", Timestamp.valueOf(DateUtils.now()));
        try {
            template.update(SQL_INSERT_GA_PROFILE_CREDENTIALS, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke saveUserCredentials({},...) with exception.", username, ex);
        }
    }

    private void deleteUserCredentials(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource("username", username);
        try {
            template.update(SQL_DELETE_GA_PROFILE_CREDENTIALS, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke deleteUserCredentials({}) with exception.", username, ex);
        }
    }

    public void saveOneFactorAuthenticationRequest(String requestId, long profileId) {
        MapSqlParameterSource params = prepareAuthenticationRequestCommonParams(requestId, profileId, TwoFactorAuthType.NONE);
        try {
            template.update(SQL_INSERT_ONE_FACTOR_AUTHENTICATION_REQUEST, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke saveOneFactorAuthenticateRequest({}, {}) with exception.", requestId, profileId, ex);
        }
    }

    public void saveTwoFactorAuthenticationRequest(
            String requestId, long profileId, TwoFactorAuthType authType, @Nullable Integer twoFactorCode
    ) {
        MapSqlParameterSource params = prepareAuthenticationRequestCommonParams(requestId, profileId, authType);
        params.addValue("two_factor_code", twoFactorCode);
        try {
            template.update(SQL_INSERT_TWO_FACTOR_AUTHENTICATION_REQUEST, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke saveOneFactorAuthenticateRequest({}, {}, {}, {}) with exception.",
                    requestId, profileId, authType, twoFactorCode, ex);
        }
    }

    @Nullable
    public AuthenticationRequest getAuthenticationRequest(String requestId) {
        MapSqlParameterSource params = new MapSqlParameterSource("request_id", requestId);
        try {
            return template.queryForObject(SQL_SELECT_AUTHENTICATION_REQUEST_BY_ID, params, (rs, i) -> new AuthenticationRequest(
                    rs.getString("request_id"),
                    RepositoryUtils.extractDateFromRS(rs, "date_create"),
                    rs.getLong("profile_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    TwoFactorAuthType.of(rs.getString("auth_type")),
                    RepositoryUtils.extractIntegerFromRS(rs, "two_factor_code")
            ));
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getAuthenticationRequest({}) with exception.", requestId, ex);
        }
        return null;
    }

    public void completedRequest(String requestId) {
        MapSqlParameterSource params = new MapSqlParameterSource("request_id", requestId);
        try {
            template.update(SQL_UPDATE_AUTHENTICATION_REQUEST, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke completedRequest({}) with exception.", requestId, ex);
        }
    }

    private static MapSqlParameterSource prepareAuthenticationRequestCommonParams(
            String requestId, long profileId, TwoFactorAuthType authType
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("request_id", requestId);
        params.addValue("date_create", Timestamp.valueOf(DateUtils.now()));
        params.addValue("profile_id", profileId);
        params.addValue("auth_type", authType.name());
        return params;
    }
}
