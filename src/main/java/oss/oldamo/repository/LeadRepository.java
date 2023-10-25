package oss.oldamo.repository;

import oss.oldamo.domain.api.LeadModel;
import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class LeadRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(LeadRepository.class);

    private static final String SQL_SELECT_IS_EXIST_LEAD_INFO = """
            select exists(select 1 from amo_crm_lead_info where lead_id = :lead_id) as is_exist
            """;

    private static final String SQL_INSERT_LEAD = """
            insert into amo_crm_lead_info(lead_id, lead_name, link, created_date, updated_date, closed_date,
                    pipeline_id, status_id, option_source_id, account_id, reject_reason,
                    select_source_id, revenue)
            values (:lead_id, :lead_name, :link, :created_date, :updated_date, :closed_date,
                    :pipeline_id, :status_id, :option_source_id, :account_id, :reject_reason,
                    :source_option_id, :revenue)
            """;

    private static final String SQL_UPDATE_LEAD = """
            update amo_crm_lead_info
            set updated_date = :updated_date,
                closed_date = :closed_date,
                pipeline_id = :pipeline_id,
                status_id = :status_id,
                option_source_id = :option_source_id,
                account_id = :account_id
            where lead_id = :lead_id
            """;

    private static final String SQL_INSERT_LEAD_CONTACT = """
            insert into amo_crm_lead_contact(lead_id, contact_id, main)
            values (:lead_id, :contact_id, :main)
            """;

    private static final String SQL_SELECT_IS_EXIST_LEAD_EVENT = """
            select exists(select 1 from amo_crm_lead_event where id = :id) as is_exist
            """;

    private static final String SQL_INSERT_LEAD_EVENT = """
            insert into amo_crm_lead_event (id, date_create, type, lead_id, user_id, created_at,
                    old_pipeline_id, new_pipeline_id, old_status_id,new_status_id)
            values (:id, :date_create, :type, :lead_id, :user_id, :created_at,
                    :old_pipeline_id, :new_pipeline_id, :old_status_id, :new_status_id)
            """;

    private static final String SQL_DELETE_LEAD = """
            update amo_crm_lead_info
            set deleted = true
            where lead_id = :lead_id
            """;

    private static final String SQL_SELECT_CONTACTS_IDS_BY_LEAD_ID = """
            select contact_id
            from amo_crm_lead_contact
            where lead_id = :lead_id
            """;

    private static final String SQL_UPDATE_LEAD_SOURCE = """
            update amo_crm_lead_info
            set option_source_id = :option_source_id, select_source_id = :source_option_id
            where lead_id = :lead_id
            """;

    public LeadRepository(JdbcTemplate template) {
        super(template);
    }

    public boolean isExistLead(long leadId) {
        MapSqlParameterSource params = new MapSqlParameterSource("lead_id", leadId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_LEAD_INFO, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistLeadInfo({}) with exception.", leadId, ex);
        }
        return false;
    }

    public boolean isExistLeadEvent(String id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_LEAD_EVENT, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistLeadEvent({}) with exception.", id, ex);
        }
        return false;
    }

    public void addLead(LeadModel leadInfo, String link, @Nullable Long sourceOption,
                        @Nullable Long sourceId, @Nullable String reason, @Nullable Long revenue) {
        MapSqlParameterSource params = prepareLeadCommonParams(leadInfo, sourceId);
        params.addValue("lead_id", leadInfo.getId());
        params.addValue("lead_name", leadInfo.getName());
        params.addValue("link", link);
        params.addValue("deleted", leadInfo.isDeleted());
        params.addValue("created_date", of(leadInfo.getCreatedDate()));
        params.addValue("reject_reason", reason);
        params.addValue("source_option_id", sourceOption);
        params.addValue("revenue", revenue);

        try {
            template.update(SQL_INSERT_LEAD, params);
        } catch (DuplicateKeyException duplicate) {
            logger.error("Invoke addLead({}) with DuplicateKeyException", leadInfo.getId());
        } catch (DataAccessException ex) {
            logger.error("Invoke addLead({}) with exception.", leadInfo, ex);
        }
    }

    public void updateLead(LeadModel leadInfo, @Nullable Long sourceId) {
        MapSqlParameterSource params = prepareLeadCommonParams(leadInfo, sourceId);
        params.addValue("lead_id", leadInfo.getId());

        try {
            template.update(SQL_UPDATE_LEAD, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateLead({}, {}) with exception.", leadInfo, sourceId, ex);
        }
    }

    public Set<Long> getLeadContactsIds(long leadId) {
        MapSqlParameterSource params = new MapSqlParameterSource("lead_id", leadId);
        try {
            return new HashSet<>(template.query(SQL_SELECT_CONTACTS_IDS_BY_LEAD_ID, params,
                    (rs, i) -> rs.getLong("contact_id")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getLeadContactsIds({}) with exception.", leadId, ex);
        }
        return Collections.emptySet();
    }

    public void addContacts(long leadId, Collection<LeadModel.LeadEmbeddedContact> contacts) {
        MapSqlParameterSource[] parameterSources = contacts
                .stream()
                .map(contact -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    params.addValue("lead_id", leadId);
                    params.addValue("contact_id", contact.id());
                    params.addValue("main", contact.main());
                    return params;
                })
                .toArray(MapSqlParameterSource[]::new);

        try {
            template.batchUpdate(SQL_INSERT_LEAD_CONTACT, parameterSources);
        } catch (DuplicateKeyException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke addContacts({}, {}) with exception.", leadId, contacts, ex);
        }
    }

    public void addLeadEvent(
            String id, String type, long leadId, long userId, ZonedDateTime dateCreate,
            @Nullable Long oldPipelineId, @Nullable Long newPipelineId,
            @Nullable Long oldStatusId, @Nullable Long newStatusId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("date_create", Timestamp.valueOf(DateUtils.now()));
        params.addValue("type", type);
        params.addValue("lead_id", leadId);
        params.addValue("user_id", userId);
        params.addValue("created_at", Timestamp.valueOf(dateCreate.toLocalDateTime()));
        params.addValue("old_pipeline_id", oldPipelineId);
        params.addValue("new_pipeline_id", newPipelineId);
        params.addValue("old_status_id", oldStatusId);
        params.addValue("new_status_id", newStatusId);

        try {
            template.update(SQL_INSERT_LEAD_EVENT, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke saveLeadEvent({}, {}, {}, {}, {}, {}, {}, {}, {}) with exception.",
                    id, type, leadId, userId, dateCreate, oldPipelineId, newPipelineId, oldStatusId, newStatusId, ex);
        }
    }

    public void deleteLead(long leadId) {
        MapSqlParameterSource params = new MapSqlParameterSource("lead_id", leadId);
        try {
            template.update(SQL_DELETE_LEAD, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke deleteLead({}) with exception.", leadId, ex);
        }
    }

    public void updateLeadSourceId(long leadId, long optionId, long sourceId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("lead_id", leadId);
        params.addValue("option_source_id", optionId);
        params.addValue("source_option_id", sourceId);
        try {
            template.update(SQL_UPDATE_LEAD_SOURCE, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateLeadSourceId({}, {}, {}) with exception.", leadId, optionId, sourceId, ex);
        }
    }

    private static MapSqlParameterSource prepareLeadCommonParams(LeadModel leadInfo, @Nullable Long sourceId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("updated_date", of(leadInfo.getUpdatedDate()));
        params.addValue("closed_date", of(leadInfo.getClosedDate()));
        params.addValue("pipeline_id", leadInfo.getPipelineId());
        params.addValue("status_id", leadInfo.getStatusId());
        params.addValue("account_id", leadInfo.getAccountId());
        params.addValue("option_source_id", sourceId);
        return params;
    }
}
