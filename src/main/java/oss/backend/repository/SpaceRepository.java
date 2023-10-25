package oss.backend.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import oss.backend.domain.space.SpaceSettings;
import oss.backend.util.DateUtils;
import oss.backend.util.RepositoryUtils;

import javax.annotation.Nullable;
import java.sql.Timestamp;

@Repository
public class SpaceRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(SpaceRepository.class);

    private static final String SQL_SELECT_NEXT_SPACE_ID = """
            select nextval('space_id')
            """;

    private static final String SQL_INSERT_SPACE = """
            insert into oss_space (id, space_name, creation_date)
            values (:id, :space_name, :creation_date)
            """;

    private static final String SQL_SELECT_SPACE_BY_ID = """
            select id,
                   space_name,
                   oss_domain,
                   creation_date,
                   confirmation_date,
                   (select client_id
                    from oss_amo_client_credentials
                    where space_id = id) as amo_client_id
            from oss_space
            where id = :id
            """;

    public SpaceRepository(JdbcTemplate template) {
        super(template);
    }

    @Nullable
    public SpaceSettings getSpaceSettings(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return template.queryForObject(SQL_SELECT_SPACE_BY_ID, params,
                    (rs, i) -> new SpaceSettings(
                            rs.getLong("id"),
                            rs.getString("space_name"),
                            rs.getString("oss_domain"),
                            RepositoryUtils.extractDateFromRS(rs, "creation_date"),
                            RepositoryUtils.extractDateFromRS(rs, "confirmation_date"),
                            rs.getString("amo_client_id")
                    ));
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getSpaceSettingsById({}) with exception.", id, ex);
        }
        return null;
    }

    @Nullable
    public Long getNextSpaceId() {
        try {
            return template.queryForObject(SQL_SELECT_NEXT_SPACE_ID, new MapSqlParameterSource(), Long.class);
        } catch (DataAccessException ex) {
            logger.error("Invoke getNextSpaceId() with exception.", ex);
        }
        return null;
    }

    public boolean createSpace(long spaceId, String spaceName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", spaceId);
        params.addValue("space_name", spaceName);
        params.addValue("creation_date", Timestamp.valueOf(DateUtils.now()));

        try {
            return template.update(SQL_INSERT_SPACE, params) > 0;
        } catch (DataAccessException ex) {
            logger.error("Invoke createSpace({}, {}) with exception.", spaceId, spaceName, ex);
        }
        return false;
    }
}
