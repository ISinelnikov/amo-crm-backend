package oss.oldamo.repository;

import oss.backend.domain.dashboard.DashboardPage;
import oss.backend.domain.dashboard.LeadDetailsInfo;
import oss.backend.repository.AbstractRepository;
import oss.backend.util.RepositoryUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class LeadSearchRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(LeadSearchRepository.class);

    private static final String SQL_SELECT_LEADS_DETAILS_INFOS_COUNT = """
            select count(lead_id) as leads_count
            from amo_crm_lead_info
            where :from_date <= created_date
              and :to_date >= created_date
              and deleted = false
            """;

    private static final String SQL_SELECT_LEADS_DETAILS = """
            select lead_id,
                   lead_name,
                   link,
                   created_date,
                   reject_reason,
                   (select pipeline.name
                    from pipeline
                    where pipeline.pipeline_id = amo_crm_lead_info.pipeline_id) as pipeline,
                   (select pipeline_status.name
                    from pipeline_status
                    where status_id = amo_crm_lead_info.status_id
                      and pipeline_id = amo_crm_lead_info.pipeline_id)          as status,
                   (select value
                    from amo_crm_custom_field_value
                    where id = option_source_id)                              as source,
                   (select exists(select 1
                                  from amo_crm_lead_event
                                  where amo_crm_lead_info.lead_id = amo_crm_lead_event.lead_id
                                    and (old_status_id in (:qualified_ids)
                                      or new_status_id in (:qualified_ids))))   as qualified,
                   (select exists(select 1
                                  from amo_crm_lead_event
                                  where amo_crm_lead_info.lead_id = amo_crm_lead_event.lead_id
                                    and (old_status_id in (:closed_ids)
                                      or new_status_id in (:closed_ids))))      as closed
            from amo_crm_lead_info
            where :from_date <= created_date
              and :to_date >= created_date
              and deleted = false
            """;

    private static final String SQL_SELECT_LEADS_DETAILS_INFOS_BY_SEARCH_VALUE = """
            and (lower(lead_id::text) like lower('%' || :search_value || '%') or lower(lead_name) like lower('%' || :search_value || '%'))
            """;

    private static final String SQL_SELECT_LEADS_DETAILS_INFOS_BY_PIPELINE_ID = """ 
            and pipeline_id = :pipeline_id
            """;

    private static final String SQL_SELECT_LEADS_DETAILS_INFOS_BY_PIPELINE_STATUS_ID = """ 
            and status_id = :status_id
            """;

    private static final String SQL_ORDER_BY_CREATED_DATE_DESC = """
            order by created_date desc
            limit :limit offset :offset
            """;

    public LeadSearchRepository(JdbcTemplate template) {
        super(template);
    }

    public DashboardPage<LeadDetailsInfo> getLeadsDetailsPage(LocalDateTime fromDate, LocalDateTime toDate,
            int page, int pageSize, @Nullable String searchValue,
            @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            Set<Long> qualifiedIds, Set<Long> closedIds) {
        int count = getLeadsDetailsInfosCount(fromDate, toDate, searchValue, pipelineId, pipelineStatusId,
                qualifiedIds, closedIds);
        Collection<LeadDetailsInfo> infos = Collections.emptyList();
        if (count != 0) {
            infos = getLeadsDetailsInfos(fromDate, toDate, page, pageSize, searchValue, pipelineId,
                    pipelineStatusId, qualifiedIds, closedIds);
        }
        return DashboardPage.of(page, pageSize, count, infos);
    }

    @SuppressWarnings("DataFlowIssue")
    private int getLeadsDetailsInfosCount(LocalDateTime fromDate, LocalDateTime toDate,
            @Nullable String searchValue,
            @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            Set<Long> qualifiedIds, Set<Long> closedIds) {
        MapSqlParameterSource params = prepareLeadSearchParams(
                fromDate,
                toDate,
                searchValue,
                pipelineId,
                pipelineStatusId,
                qualifiedIds,
                closedIds
        );

        String query = prepareQuery(
                SQL_SELECT_LEADS_DETAILS_INFOS_COUNT,
                searchValue,
                pipelineId,
                pipelineStatusId
        );

        try {
            return template.queryForObject(query, params, (rs, i) -> rs.getInt("leads_count"));
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadsDetailsInfosCount({}, {}, {}, {}, {}, {}, {}) with exception.",
                    fromDate, toDate, searchValue, pipelineId, pipelineStatusId, qualifiedIds, closedIds, ex);
        }
        return 0;
    }

    private Collection<LeadDetailsInfo> getLeadsDetailsInfos(LocalDateTime fromDate, LocalDateTime toDate,
            int page, int pageSize, @Nullable String searchValue,
            @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            Set<Long> qualifiedIds, Set<Long> closedIds) {
        MapSqlParameterSource params = prepareLeadSearchParams(
                fromDate,
                toDate,
                searchValue,
                pipelineId,
                pipelineStatusId,
                qualifiedIds,
                closedIds
        );
        params.addValue("limit", pageSize);
        params.addValue("offset", (page - 1) * pageSize);

        String query = prepareQuery(
                SQL_SELECT_LEADS_DETAILS,
                searchValue,
                pipelineId,
                pipelineStatusId
        ) + SQL_ORDER_BY_CREATED_DATE_DESC;
        try {
            return template.query(query, params, (rs, i) -> new LeadDetailsInfo(
                    rs.getLong("lead_id"),
                    rs.getString("lead_name"),
                    rs.getString("link"),
                    rs.getString("pipeline"),
                    rs.getString("status"),
                    rs.getString("source"),
                    rs.getBoolean("qualified"),
                    rs.getBoolean("closed"),
                    rs.getString("reject_reason"),
                    RepositoryUtils.extractDateFromRS(rs, "created_date")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadItems({}, {}, {}, {}, {}) with exception.",
                    fromDate, toDate, qualifiedIds, closedIds, pipelineId, ex);
        }
        return Collections.emptyList();
    }

    private static String prepareQuery(String query, @Nullable String searchValue,
                                       @Nullable Long pipelineId, @Nullable Long pipelineStatusId) {
        if (StringUtils.hasText(searchValue)) {
            query += SQL_SELECT_LEADS_DETAILS_INFOS_BY_SEARCH_VALUE;
        }
        if (pipelineId != null) {
            query += SQL_SELECT_LEADS_DETAILS_INFOS_BY_PIPELINE_ID;
        }
        if (pipelineStatusId != null) {
            query += SQL_SELECT_LEADS_DETAILS_INFOS_BY_PIPELINE_STATUS_ID;
        }
        return query;
    }

    private static MapSqlParameterSource prepareLeadSearchParams(LocalDateTime fromDate, LocalDateTime toDate,
            @Nullable String searchValue, @Nullable Long pipelineId, @Nullable Long pipelineStatusId,
            Set<Long> qualifiedIds, Set<Long> closedIds) {
        MapSqlParameterSource params = prepareFromToDatesParams(fromDate, toDate);
        params.addValue("qualified_ids", new ArrayList<>(qualifiedIds));
        params.addValue("closed_ids", new ArrayList<>(closedIds));
        if (StringUtils.hasText(searchValue)) {
            params.addValue("search_value", searchValue);
        }
        if (pipelineId != null) {
            params.addValue("pipeline_id", pipelineId);
        }
        if (pipelineStatusId != null) {
            params.addValue("status_id", pipelineStatusId);
        }
        return params;
    }
}
