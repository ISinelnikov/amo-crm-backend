package oss.newamo.repository.impl;

import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;
import oss.newamo.domain.user.AmoCrmUser;
import oss.newamo.domain.user.User;
import oss.newamo.repository.UserRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class PgUserRepository extends AbstractRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(PgUserRepository.class);

    private static final String SQL_SELECT_IS_EXIST_USER = """
            select exists(select 1 from oss_amo_user where client_id = :client_id and id = :user_id) as is_exist
            """;

    private static final String SQL_INSERT_USER = """
            insert into oss_amo_user (client_id, id, name, email, creation_date)
            values (:client_id, :id, :name, :email, :creation_date)
            """;

    private static final String SQL_SELECT_USERS = """
            select id, name, email
            from oss_amo_user
            where client_id = :client_id
            """;

    public PgUserRepository(JdbcTemplate template) {
        super(template);
    }

    @Override
    public boolean isExistUser(String clientId, long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("user_id", userId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_USER, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistUser({}, {}) with exception.", clientId, userId, ex);
        }
        return false;
    }

    @Override
    public Collection<User> getUsers(String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource("client_id", clientId);
        try {
            return template.query(SQL_SELECT_USERS, params, (rs, i) -> new User(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getUsers({}) with exception.", clientId, ex);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveUsers(String clientId, Collection<AmoCrmUser> users) {
        try {
            template.batchUpdate(SQL_INSERT_USER, users.stream().map(user -> {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("client_id", clientId);
                params.addValue("id", user.getId());
                params.addValue("name", user.getName());
                params.addValue("email", user.getEmail());
                params.addValue("creation_date", Timestamp.valueOf(DateUtils.now()));
                return params;
            }).toArray(MapSqlParameterSource[]::new));
        } catch (DataAccessException ex) {
            logger.error("Invoke saveUsers({}, {}) with exception.", clientId, users, ex);
        }
    }

    @Override
    public void updateUserVisible(String clientId, long userId, boolean hidden) {

    }
}
