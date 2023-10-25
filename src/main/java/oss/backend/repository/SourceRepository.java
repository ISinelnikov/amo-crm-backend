package oss.backend.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import oss.backend.domain.source.CallSourcePattern;
import oss.backend.domain.source.GroupInfo;
import oss.backend.domain.source.VisitPatternItem;
import oss.backend.domain.source.VisitSourcePattern;
import oss.backend.util.MappingUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Repository
public class SourceRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(SourceRepository.class);

    private static final String SQL_SELECT_LEAD_SOURCE_INFO = """
            select lead_id
            from amo_crm_lead_info
            where option_source_id is null
              and deleted = false
              and pipeline_id in (3340174, 6502242)
              and lead_id > 30616999
            order by created_date desc
            """;

    private static final String SQL_SELECT_CALL_SOURCE_PATTERN = """
            select pattern_id, source_group_info.group_id, group_name, country_code, phone_number
            from call_source_pattern, source_group_info
            where call_source_pattern.group_id = source_group_info.group_id
            """;

    private static final String SQL_INSERT_VISIT_SOURCE_PATTERN = """
            insert into visit_source_pattern(group_id, priority, items)
            values (:group_id, :priority, :items)
            """;

    private static final String SQL_SELECT_VISIT_SOURCE_PATTERN = """
            select pattern_id,
                   source_group_info.group_id,
                   priority,
                   group_name,
                   items,
                   select_id,
                   select_option_id
            from visit_source_pattern,
                 source_group_info
            where visit_source_pattern.group_id = source_group_info.group_id
            """;

    private static final String SQL_SELECT_GROUP_INFO_BY_ID = """
            select group_id, group_name, select_id, select_option_id
            from source_group_info
            where group_id = :group_id
            """;

    public SourceRepository(JdbcTemplate template) {
        super(template);
    }

    @Nullable
    public GroupInfo getGroupInfoById(long groupId) {
        MapSqlParameterSource params = new MapSqlParameterSource("group_id", groupId);
        try {
            return template.queryForObject(SQL_SELECT_GROUP_INFO_BY_ID, params, (rs, i) -> new GroupInfo(
                    rs.getLong("group_id"),
                    rs.getString("group_name"),
                    rs.getLong("select_id"),
                    rs.getLong("select_option_id")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getGroupInfoById({}) with exception.", groupId, ex);
        }
        return null;
    }

    public Collection<VisitSourcePattern> getVisitSourcePatterns() {
        try {
            return template.query(SQL_SELECT_VISIT_SOURCE_PATTERN, (rs, i) -> new VisitSourcePattern(
                    rs.getLong("pattern_id"),
                    rs.getLong("group_id"),
                    rs.getInt("priority"),
                    rs.getString("group_name"),
                    MappingUtils.parseJsonToCollection(rs.getString("items"), VisitPatternItem.class),
                    rs.getInt("select_id"),
                    rs.getInt("select_option_id")));
        } catch (DataAccessException ex) {
            logger.error("Invoke getCallSourceInfos() with exception.", ex);
        }
        return Collections.emptyList();
    }

    public Collection<CallSourcePattern> getCallSourcePatterns() {
        try {
            return template.query(SQL_SELECT_CALL_SOURCE_PATTERN, (rs, i) -> new CallSourcePattern(
                    rs.getLong("pattern_id"),
                    rs.getLong("group_id"),
                    rs.getString("group_name"),
                    rs.getString("country_code"),
                    rs.getString("phone_number")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getCallSourceInfos() with exception.", ex);
        }
        return Collections.emptyList();
    }

    public Set<Long> getCurrentSourcesLeads() {
        try {
            return new HashSet<>(template.query(SQL_SELECT_LEAD_SOURCE_INFO, (rs, i) -> rs.getLong("lead_id")));
        } catch (DataAccessException ex) {
            logger.error("Invoke getCurrentSourcesLeads() with exception.", ex);
        }
        return Collections.emptySet();
    }

    public void addVisitPattern(long groupId, int priority, Collection<VisitPatternItem> items) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("group_id", groupId);
        params.addValue("priority", priority);
        params.addValue("items", MappingUtils.convertObjectToJson(items));
        try {
            template.update(SQL_INSERT_VISIT_SOURCE_PATTERN, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke insertVisitPattern({}, {}) with exception.", groupId, items, ex);
        }
    }
}
