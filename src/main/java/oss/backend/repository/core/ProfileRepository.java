package oss.backend.repository.core;

import oss.backend.domain.Profile;
import oss.backend.repository.AbstractRepository;
import oss.backend.security.TwoFactorAuthType;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import oss.backend.util.RepositoryUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class ProfileRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProfileRepository.class);

    private static final String SQL_SELECT_NEXT_PROFILE_ID = """
            select nextval('profile_id')
            """;

    private static final String SQL_SELECT_IS_EXIST_EMAIL = """
            select exists(select 1 from profile where lower(username) = lower(:email)) as is_exist
            """;

    private static final String SQL_SELECT_PROFILE = """
            select id,
                   first_name,
                   last_name,
                   username,
                   password,
                   avatar_base64,
                   auth_type,
                   email_confirmation_date,
                   space_id
            from profile
            """;

    private static final String SQL_SELECT_PROFILE_BY_USERNAME = SQL_SELECT_PROFILE + """
            where lower(username) = lower(:username)
            """;

    private static final String SQL_SELECT_PROFILE_BY_ID = SQL_SELECT_PROFILE + """
            where id = :id
            """;

    private static final String SQL_UPDATE_PROFILE_AVATAR = """
            update profile
            set avatar_base64 = :data
            where id = :profile_id
            """;

    private static final String SQL_UPDATE_PROFILE_PASSWORD = """
            update profile
            set password = :password
            where id = :profile_id
            """;

    private static final String SQL_UPDATE_PROFILE = """
            update profile
            set first_name = :first_name, last_name = :last_name
            where id = :profile_id
            """;

    private static final String SQL_UPDATE_PROFILE_AUTH_TYPE = """
            update profile
            set auth_type = :auth_type
            where id = :profile_id
            """;

    private static final String SQL_INSERT_PROFILE = """
            insert into profile (id, first_name, last_name, username, password, creation_date, space_id)
            values (:id, :first_name, :last_name, :username, :password, :creation_date, :space_id)
            """;

    public ProfileRepository(JdbcTemplate template) {
        super(template);
    }

    @Nullable
    public Long getNextProfileId() {
        try {
            return template.queryForObject(SQL_SELECT_NEXT_PROFILE_ID, new MapSqlParameterSource(), Long.class);
        } catch (DataAccessException ex) {
            logger.error("Invoke getNextProfileId() with exception.", ex);
        }
        return null;
    }

    @Nullable
    public Profile getProfileByUsername(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource("username", username);
        try {
            return template.queryForObject(SQL_SELECT_PROFILE_BY_USERNAME, params, getProfileRowMapper());
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getProfileByUsername({}) with exception.", username, ex);
        }
        return null;
    }

    @Nullable
    public Profile getProfileById(long profileId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", profileId);
        try {
            return template.queryForObject(SQL_SELECT_PROFILE_BY_ID, params, getProfileRowMapper());
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getProfileById({}) with exception.", profileId, ex);
        }
        return null;
    }

    public void addProfileAvatar(long profileId, String data) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("profile_id", profileId);
        params.addValue("data", data);
        try {
            template.update(SQL_UPDATE_PROFILE_AVATAR, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke addProfileAvatar({}, ...) with exception.", profileId, ex);
        }
    }

    public void deleteProfileAvatar(long profileId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("profile_id", profileId);
        params.addValue("data", null);
        try {
            template.update(SQL_UPDATE_PROFILE_AVATAR, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke deleteProfileAvatar({}) with exception.", profileId, ex);
        }
    }

    public void updatePassword(long profileId, String newPassword) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("profile_id", profileId);
        params.addValue("password", newPassword);
        try {
            template.update(SQL_UPDATE_PROFILE_PASSWORD, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updatePassword({}, ***) with exception.", profileId, ex);
        }
    }

    public void updateProfile(long profileId, String firstName, String lastName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("profile_id", profileId);
        params.addValue("first_name", firstName);
        params.addValue("last_name", lastName);
        try {
            template.update(SQL_UPDATE_PROFILE, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateProfile({}, {}, {}) with exception.", profileId, firstName, lastName, ex);
        }
    }

    public void updateTwoFactorAuthType(long profileId, TwoFactorAuthType authType) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("profile_id", profileId);
        params.addValue("auth_type", authType.name());
        try {
            template.update(SQL_UPDATE_PROFILE_AUTH_TYPE, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateTwoFactorAuthType({}, {}) with exception.", profileId, authType, ex);
        }
    }

    public boolean isExistEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_EMAIL, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistEmail({}) with exception.", email, ex);
        }
        return false;
    }

    public boolean createProfile(long profileId, String firstName, String lastName, String email, String password, long spaceId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", profileId);
        params.addValue("first_name", firstName);
        params.addValue("last_name", lastName);
        params.addValue("username", email);
        params.addValue("password", password);
        params.addValue("creation_date", Timestamp.valueOf(LocalDateTime.now()));
        params.addValue("space_id", spaceId);
        try {
            return template.update(SQL_INSERT_PROFILE, params) > 0;
        } catch (DataAccessException ex) {
            logger.error("Invoke createProfile({}, {}, {}, {}, ***, {}) with exception.",
                    profileId, firstName, lastName, email, spaceId, ex);
        }
        return false;
    }

    private static RowMapper<Profile> getProfileRowMapper() {
        return (rs, i) -> new Profile(
                rs.getLong("id"),
                rs.getLong("space_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("avatar_base64"),
                TwoFactorAuthType.of(rs.getString("auth_type")),
                RepositoryUtils.extractDateFromRS(rs, "email_confirmation_date")
        );
    }
}
