package oss.backend.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractRepository {
    protected final NamedParameterJdbcTemplate template;

    public AbstractRepository(JdbcTemplate template) {
        this.template = new NamedParameterJdbcTemplate(Objects.requireNonNull(template, "Template can't be null."));
    }

    @Nullable
    public static Timestamp of(@Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
    }

    protected static MapSqlParameterSource prepareCommonParams(
            LocalDateTime fromDate, LocalDateTime toDate, @Nullable Long pipelineId
    ) {
        MapSqlParameterSource params = prepareFromToDatesParams(fromDate, toDate);
        params.addValue("pipeline_id", pipelineId);
        return params;
    }

    protected static MapSqlParameterSource prepareFromToDatesParams(
            LocalDateTime fromDate, LocalDateTime toDate
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("from_date", Timestamp.valueOf(fromDate));
        params.addValue("to_date", Timestamp.valueOf(toDate));
        return params;
    }
}
