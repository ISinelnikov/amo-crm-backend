package oss.backend.repository;

import oss.backend.domain.dashboard.LeadSourceItem;
import oss.backend.domain.dashboard.LeadStatusEventInfo;
import oss.backend.domain.dashboard.LeadsKpiDetails;
import oss.backend.domain.dashboard.LeadsKpiDetailsDay;
import oss.backend.domain.dashboard.PipelineStatusInfoItem;
import oss.backend.util.RepositoryUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
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
public class DashboardRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(DashboardRepository.class);

    private static final String SQL_SELECT_ALL_LEAD_ITEMS = """
            select (select value
                    from amo_crm_custom_field_value
                    where id = option_source_id) as name,
                   status_state.value
            from (select option_source_id, count(*) as value
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false
                  group by option_source_id) as status_state
            """;

    private static final String SQL_SELECT_ALL_LEAD_SOURCE_ITEMS_BY_STATUS_IDS = """
            select (select value
                    from amo_crm_custom_field_value
                    where id = option_source_id) as name,
                   status_state.value
            from (select option_source_id, count(*) as value
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and lead_id in (select distinct event.lead_id
                                    from amo_crm_lead_event event
                                    where (event.old_pipeline_id = :pipeline_id and event.old_status_id in (:status_ids))
                                       or (event.new_pipeline_id = :pipeline_id and event.new_status_id in (:status_ids)))
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false
                  group by option_source_id) as status_state
            """;

    private static final String SQL_SELECT_PIPELINE_INFO_ITEMS = """
            select pipeline_status.order_id, pipeline_status.status_id, pipeline_status.name, leads_info.value
            from (select status_id, count(*) as value
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false
                  group by status_id) leads_info
                     right join pipeline_status on pipeline_status.status_id = leads_info.status_id
            where pipeline_status.pipeline_id = :pipeline_id
            """;

    private static final String SQL_SELECT_COUNT_LEADS_BY_STATUS_IDS = """
            select count(distinct amo_crm_lead_info.lead_id)
            from amo_crm_lead_event,
                 amo_crm_lead_info
            where user_id = :user_id
              and ((old_pipeline_id = :pipeline_id and old_status_id in (:status_ids))
                or (new_pipeline_id = :pipeline_id and new_status_id in (:status_ids)))
              and :from_date <= amo_crm_lead_event.created_at
              and :to_date >= amo_crm_lead_event.created_at
              and amo_crm_lead_event.lead_id = amo_crm_lead_info.lead_id
              and amo_crm_lead_info.deleted = false
            """;

    private static final String SQL_SELECT_EVENT_INFO = """
            select amo_crm_lead_info.lead_id,
                   amo_crm_lead_info.deleted,
                   amo_crm_lead_info.created_date                                                    as date_create,
                   (select name from amo_crm_user_info where id = user_id)                           as name,
                   created_at,
                   (select pipeline.name from pipeline where pipeline.pipeline_id = old_pipeline_id) as old_pipeline,
                   (select pipeline.name from pipeline where pipeline.pipeline_id = new_pipeline_id) as new_pipeline,
                   (select pipeline_status.name
                    from pipeline_status
                    where status_id = old_status_id
                      and pipeline_id = old_pipeline_id)                                             as old_status,
                   (select pipeline_status.name
                    from pipeline_status
                    where status_id = new_status_id
                      and pipeline_id = new_pipeline_id)                                             as new_status
            from amo_crm_lead_event,
                 amo_crm_lead_info
            where amo_crm_lead_event.lead_id = amo_crm_lead_info.lead_id
              and :from_date <= amo_crm_lead_event.created_at
              and :to_date >= amo_crm_lead_event.created_at
            """;

    private static final String SQL_SELECT_EVENT_INFO_PIPELINE_ID_CONDITION = """
            and (old_pipeline_id = :pipeline_id or new_pipeline_id = :pipeline_id)
            """;

    private static final String SQL_SELECT_EVENT_INFO_USER_ID_CONDITION = """
            and user_id = :user_id
            """;

    private static final String SQL_ORDER_BY_CREATED_AT_DESC = """
            order by created_at desc
            """;

    private static final String SQL_SELECT_REVENUE_BY_MANAGER_ID = """
            select sum(revenue) as revenue
            from amo_crm_lead_info
            where lead_id in (select distinct lead_id
                              from amo_crm_lead_event
                              where user_id = :user_id
                                and ((old_pipeline_id = :pipeline_id and old_status_id in (:status_ids))
                                  or (new_pipeline_id = :pipeline_id and new_status_id in (:status_ids)))
                                and :from_date <= amo_crm_lead_event.created_at
                                and :to_date >= amo_crm_lead_event.created_at)
              and deleted = false
            """;

    private static final String SQL_SELECT_LEADS_KPI_DETAILS = """
            select qualified_leads, qualified_leads_required, leads, leads_required
            from (select count(lead_id) as qualified_leads
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and lead_id in (select distinct event.lead_id
                                    from amo_crm_lead_event event
                                    where (event.old_pipeline_id = :pipeline_id and event.old_status_id in (:qualified_ids))
                                       or (event.new_pipeline_id = :pipeline_id and event.new_status_id in (:qualified_ids)))
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false) o1,
                 (select count(lead_id) as leads
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false) o2,
                 (select qualified_leads_required, leads_required
                  from lead_kpi_details details
                  where pipeline_id = :pipeline_id
                    and :from_date <= details.from_date
                    and :to_date >= details.to_date) o3
            """;

    private static final String SQL_SELECT_LEAD_KPI_DAY_DETAILS = """
            select all_leads.date,
                   to_char(all_leads.date, 'DD.MM.YYYY')                                                     as name,
                   leads_by_day,
                   qualified_by_day,
                   ceil(leads_required /
                        extract(days from date_trunc('month', all_leads.date) + interval '1 month - 1 day')) as leads_required,
                   ceil(qualified_leads_required /
                        extract(days from date_trunc('month', all_leads.date) + interval '1 month - 1 day')) as qualified_required
            from (select date_trunc('day', amo_crm_lead_info.created_date) as date,
                         count(lead_id)                                    as leads_by_day
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false
                  group by 1) all_leads
                     left join
                 (select date_trunc('day', amo_crm_lead_info.created_date) as date,
                         count(lead_id)                                    as qualified_by_day
                  from amo_crm_lead_info
                  where pipeline_id = :pipeline_id
                    and lead_id in (select distinct event.lead_id
                                    from amo_crm_lead_event event
                                    where (event.old_pipeline_id = :pipeline_id and event.old_status_id in (:qualified_ids))
                                       or (event.new_pipeline_id = :pipeline_id and event.new_status_id = (:qualified_ids)))
                    and :from_date <= created_date
                    and :to_date >= created_date
                    and deleted = false
                  group by 1) qualified_leads
                 on all_leads.date = qualified_leads.date
                     left join lead_kpi_details
                               on lead_kpi_details.from_date <= all_leads.date
                                   and lead_kpi_details.to_date >= all_leads.date
                                   and lead_kpi_details.pipeline_id = :pipeline_id
            order by 1
            """;

    public DashboardRepository(JdbcTemplate template) {
        super(template);
    }

    public LeadsKpiDetails getLeadsKpiDetails(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> qualifiedIds
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        params.addValue("qualified_ids", new ArrayList<>(qualifiedIds));

        try {
            return template.queryForObject(SQL_SELECT_LEADS_KPI_DETAILS, params, (rs, i) -> new LeadsKpiDetails(
                    rs.getLong("leads"),
                    rs.getLong("leads_required"),
                    rs.getLong("qualified_leads"),
                    rs.getLong("qualified_leads_required")
            ));
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadsKpiDetails({}, {}, {}, {}) with exception.", fromDate, toDate, pipelineId, qualifiedIds, ex);
        }
        return LeadsKpiDetails.empty();
    }

    public Collection<LeadsKpiDetailsDay> getLeadsKpiDayDetails(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> qualifiedIds
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        params.addValue("qualified_ids", new ArrayList<>(qualifiedIds));

        try {
            return template.query(SQL_SELECT_LEAD_KPI_DAY_DETAILS, params, (rs, i) -> LeadsKpiDetailsDay.of(
                    rs.getString("name"),
                    rs.getLong("leads_by_day"),
                    rs.getLong("leads_required"),
                    rs.getLong("qualified_by_day"),
                    rs.getLong("qualified_required")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadKpiDayDetails({}, {}, {}, {}) with exception.",
                    fromDate, toDate, pipelineId, qualifiedIds, ex);
        }
        return Collections.emptyList();
    }

    public Collection<LeadStatusEventInfo> getLeadStatusEventsInfos(
            LocalDateTime fromDate, LocalDateTime toDate, @Nullable Long pipelineId, @Nullable Long userId
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("from_date", Timestamp.valueOf(fromDate));
        params.addValue("to_date", Timestamp.valueOf(toDate));

        String sql = SQL_SELECT_EVENT_INFO;
        if (pipelineId != null) {
            sql += SQL_SELECT_EVENT_INFO_PIPELINE_ID_CONDITION;
            params.addValue("pipeline_id", pipelineId);
        }
        if (userId != null) {
            sql += SQL_SELECT_EVENT_INFO_USER_ID_CONDITION;
            params.addValue("user_id", userId);
        }
        sql += SQL_ORDER_BY_CREATED_AT_DESC;

        try {
            return template.query(sql, params, (rs, i) -> new LeadStatusEventInfo(
                    rs.getLong("lead_id"),
                    rs.getString("name"),
                    rs.getString("old_pipeline"),
                    rs.getString("new_pipeline"),
                    rs.getString("old_status"),
                    rs.getString("new_status"),
                    rs.getBoolean("deleted"),
                    RepositoryUtils.extractDateFromRS(rs, "date_create"),
                    RepositoryUtils.extractDateFromRS(rs, "created_at")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadStatusEventsInfos({}, {}, {}, {}) with exception.", fromDate, toDate, pipelineId, userId, ex);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("DataFlowIssue")
    public int getLeadsCountByManagerId(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> statusIds, long userId
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        params.addValue("status_ids", new ArrayList<>(statusIds));
        params.addValue("user_id", userId);

        try {
            return template.queryForObject(SQL_SELECT_COUNT_LEADS_BY_STATUS_IDS, params, Integer.class);
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadsCountByManagerId({}, {}, {}, {}, {}) with exception.",
                    userId, fromDate, toDate, pipelineId, statusIds, ex);
        }
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public long getRevenueByManagerId(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> statusIds, long userId
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        params.addValue("status_ids", statusIds);
        params.addValue("user_id", userId);

        try {
            return template.queryForObject(SQL_SELECT_REVENUE_BY_MANAGER_ID, params,
                    (rs, i) -> rs.getLong("revenue"));
        } catch (DataAccessException ex) {
            logger.error("Invoke getRevenueByManagerId({}, {}, {}, {}, {}) with exception.",
                    userId, fromDate, toDate, pipelineId, statusIds, ex);
        }
        return 0;
    }

    public Collection<LeadSourceItem> getAllLeadItems(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        try {
            return template.query(SQL_SELECT_ALL_LEAD_ITEMS, params, getLeadSourceItemRowMapper());
        } catch (DataAccessException ex) {
            logger.error("Invoke getAllLeadItems({}, {}, {}) with exception.", fromDate, toDate, pipelineId, ex);
        }
        return Collections.emptyList();
    }

    public Collection<LeadSourceItem> getLeadSourceItemsByStatusIds(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId, Set<Long> statusIds
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        params.addValue("status_ids", new ArrayList<>(statusIds));
        try {
            return template.query(SQL_SELECT_ALL_LEAD_SOURCE_ITEMS_BY_STATUS_IDS, params, getLeadSourceItemRowMapper());
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadSourceItemsByStatusIds({}, {}, {}, {}) with exception.",
                    fromDate, toDate, pipelineId, statusIds, ex);
        }
        return Collections.emptyList();
    }

    public Collection<PipelineStatusInfoItem> getDashboardPipelineInfoItems(
            LocalDateTime fromDate, LocalDateTime toDate, long pipelineId
    ) {
        MapSqlParameterSource params = prepareCommonParams(fromDate, toDate, pipelineId);
        try {
            return template.query(SQL_SELECT_PIPELINE_INFO_ITEMS, params, (rs, i) -> new PipelineStatusInfoItem(
                    rs.getLong("order_id"),
                    rs.getLong("status_id"),
                    rs.getString("name"),
                    rs.getInt("value")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getDashboardPipelineItems({}, {}, {}) with exception.", fromDate, toDate, pipelineId, ex);
        }
        return Collections.emptyList();
    }

    private static RowMapper<LeadSourceItem> getLeadSourceItemRowMapper() {
        return (rs, i) -> new LeadSourceItem(
                rs.getString("name"),
                rs.getInt("value")
        );
    }
}
