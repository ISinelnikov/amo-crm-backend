package oss.newamo.repository.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;
import oss.backend.util.RepositoryUtils;
import oss.newamo.domain.pipeline.Pipeline;
import oss.newamo.domain.pipeline.AmoCrmPipeline;
import oss.newamo.domain.pipeline.PipelineSettings;
import oss.newamo.domain.pipeline.status.AmoCrmPipelineStatus;
import oss.newamo.domain.pipeline.status.PipelineStatus;
import oss.newamo.repository.PipelineRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

@Repository
public class PgPipelineRepository extends AbstractRepository implements PipelineRepository {
    private static final Logger logger = LoggerFactory.getLogger(PgPipelineRepository.class);

    private static final String SQL_SELECT_IS_EXIST_PIPELINE = """
            select exists(select 1 from oss_amo_pipeline where client_id = :client_id and id = :pipeline_id) as is_exist
            """;

    private static final String SQL_SELECT_IS_EXIST_PIPELINE_STATUS = """
            select exists(select 1 from oss_amo_pipeline_status where client_id = :client_id and pipeline_id = :pipeline_id and id = :status_id) as is_exist
            """;

    private static final String SQL_INSERT_PIPELINE = """
            insert into oss_amo_pipeline (client_id, id, name, sort_id, owner_id, creation_date)
            values (:client_id, :id, :name, :sort_id, :owner_id, :creation_date)
            """;

    private static final String SQL_INSERT_PIPELINE_STATUSES = """
            insert into oss_amo_pipeline_status (client_id, id, pipeline_id, name, color, unsorted, editable, sort_id, owner_id, creation_date)
            values (:client_id, :id, :pipeline_id, :name, :color, :unsorted, :editable, :sort_id, :owner_id, :creation_date)
            """;

    private static final String SQL_DELETE_PIPELINE_STATUSES = """
            delete from oss_amo_pipeline_status where client_id = :client_id and pipeline_id = :pipeline_id
            """;

    private static final String SQL_SELECT_PIPELINES = """
            select id,
                   name,
                   alias,
                   sort_id,
                   (select count(status.id)
                    from oss_amo_pipeline_status status
                    where status.pipeline_id = oss_amo_pipeline.id
                      and status.client_id = :client_id) as statuses_count,
                   creation_date
            from oss_amo_pipeline
            where client_id = :client_id
            """;

    private static final String SQL_SELECT_PIPELINE_STATUSES = """
            select id, name, alias, sort_id, color, creation_date
            from oss_amo_pipeline_status
            where client_id = :client_id and pipeline_id = :pipeline_id
            """;

    private static final String SQL_UPDATE_PIPELINE_VISIBLE = """
            update oss_amo_pipeline set hidden = :hidden
            where client_id = :client_id and id = :pipeline_id
            """;

    private static final String SQL_UPDATE_PIPELINE_ALIAS = """
            update oss_amo_pipeline set alias = :alias
            where client_id = :client_id and id = :pipeline_id
            """;

    private static final String SQL_UPDATE_PIPELINE_STATUS_ALIAS = """
            update oss_amo_pipeline_status set alias = :alias
            where client_id = :client_id and pipeline_id = :pipeline_id and id = :status_id
            """;

    public PgPipelineRepository(JdbcTemplate template) {
        super(template);
    }

    @Override
    public boolean isExistPipeline(String clientId, long pipelineId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_PIPELINE, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistPipeline({}, {}) with exception.", clientId, pipelineId, ex);
        }
        return false;
    }

    @Override
    public boolean isExistPipelineStatus(String clientId, long pipelineId, long statusId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        params.addValue("status_id", statusId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_PIPELINE_STATUS, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistPipelineStatus({}, {}, {}) with exception.", clientId, pipelineId, statusId, ex);
        }
        return false;
    }

    @Override
    public Collection<PipelineSettings> getPipelinesSettings(String clientId) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Pipeline> getPipelines(String clientId) {
        MapSqlParameterSource params = new MapSqlParameterSource("client_id", clientId);
        try {
            return template.query(SQL_SELECT_PIPELINES, params,
                    (rs, i) -> new Pipeline(rs.getLong("id"), ObjectUtils.firstNonNull(rs.getString("alias"), rs.getString("name")),
                            rs.getLong("sort_id"), rs.getLong("statuses_count"),
                            RepositoryUtils.extractDateFromRS(rs, "creation_date")));
        } catch (DataAccessException ex) {
            logger.error("Invoke getPipelines({}) with exception.", clientId, ex);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<PipelineStatus> getPipelineStatuses(String clientId, long pipelineId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        try {
            return template.query(SQL_SELECT_PIPELINE_STATUSES, params, (rs, i) -> new PipelineStatus(rs.getLong("id"),
                    ObjectUtils.firstNonNull(rs.getString("alias"), rs.getString("name")), rs.getLong("sort_id"),
                    rs.getString("color"), RepositoryUtils.extractDateFromRS(rs, "creation_date")));
        } catch (DataAccessException ex) {
            logger.error("Invoke getPipelineStatuses({}, {}) with exception.", clientId, pipelineId, ex);
        }
        return Collections.emptyList();
    }

    @Override
    public void savePipelines(String clientId, Collection<AmoCrmPipeline> pipelines) {
        try {
            template.batchUpdate(SQL_INSERT_PIPELINE, pipelines.stream().map(pipeline -> {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("client_id", clientId);
                params.addValue("id", pipeline.getId());
                params.addValue("name", pipeline.getName());
                params.addValue("sort_id", pipeline.getSortId());
                params.addValue("owner_id", pipeline.getOwnerId());
                params.addValue("creation_date", Timestamp.valueOf(DateUtils.now()));
                return params;
            }).toArray(MapSqlParameterSource[]::new));
        } catch (DataAccessException ex) {
            logger.error("Invoke savePipelines({}, {}) with exception.", clientId, pipelines, ex);
        }
    }

    @Override
    public void savePipelineStatuses(String clientId, Collection<AmoCrmPipelineStatus> statuses) {
        try {
            template.batchUpdate(SQL_INSERT_PIPELINE_STATUSES, statuses.stream().map(status -> {
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("client_id", clientId);
                params.addValue("id", status.getId());
                params.addValue("pipeline_id", status.getPipelineId());
                params.addValue("name", status.getName());
                params.addValue("color", status.getColor());
                params.addValue("unsorted", status.isUnsorted());
                params.addValue("editable", status.isEditable());
                params.addValue("sort_id", status.getSortId());
                params.addValue("owner_id", status.getOwnerId());
                params.addValue("creation_date", Timestamp.valueOf(DateUtils.now()));
                return params;
            }).toArray(MapSqlParameterSource[]::new));
        } catch (DataAccessException ex) {
            logger.error("Invoke savePipelineStatuses({}, {}) with exception.", clientId, statuses, ex);
        }
    }

    @Override
    public void removePipelineStatuses(String clientId, long pipelineId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);

        try {
            template.update(SQL_DELETE_PIPELINE_STATUSES, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke removePipelineStatuses({}, {}) with exception.", clientId, pipelineId, ex);
        }
    }

    @Override
    public void updatePipelineVisible(String clientId, long pipelineId, boolean hidden) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        params.addValue("hidden", hidden);

        try {
            template.update(SQL_UPDATE_PIPELINE_VISIBLE, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updatePipelineVisible({}, {}, {}) with exception.", clientId, pipelineId, hidden, ex);
        }
    }

    @Override
    public void updatePipelineAlias(String clientId, long pipelineId, @Nullable String alias) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        params.addValue("alias", alias);

        try {
            template.update(SQL_UPDATE_PIPELINE_ALIAS, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updatePipelineAlias({}, {}, {}) with exception.", clientId, pipelineId, alias, ex);
        }
    }

    @Override
    public void updatePipelineStatusAlias(String clientId, long pipelineId, long statusId, @Nullable String alias) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("pipeline_id", pipelineId);
        params.addValue("status_id", statusId);
        params.addValue("alias", alias);

        try {
            template.update(SQL_UPDATE_PIPELINE_STATUS_ALIAS, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updatePipelineStatusAlias({}, {}, {}, {}) with exception.",
                    clientId, pipelineId, statusId, alias, ex);
        }
    }
}
