package oss.bot.repository;

import oss.backend.repository.AbstractRepository;
import oss.backend.util.DateUtils;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import oss.bot.BotReferrer;
import oss.bot.BotUserDetails;
import oss.bot.ReferralLead;

@Repository
public class ApiBotRepository extends AbstractRepository {
    private static final Logger logger = LoggerFactory.getLogger(ApiBotRepository.class);

    private static final String SQL_SELECT_IS_EXIST_SESSION = """
            select exists(select 1 from bot_help_user_details where bot_help_user_id = :bot_help_user_id) as is_exist
            """;

    private static final String SQL_SELECT_USER_ID_BY_ACCESS_TOKEN = """
            select user_id
            from bot_help_user_details
            where access_token = :access_token
              and user_type = 'INTERNAL'
            """;

    private static final String SQL_SELECT_REFERER_ID_BY_CODE = """
            select user_id
            from bot_help_user_details
            where referral_code = :referral_code
            """;

    private static final String SQL_INSERT_BOT_USER_SESSION = """
            update bot_help_user_details set bot_help_user_id = :bot_help_user_id where user_id = :user_id
            """;

    private static final String SQL_SELECT_BOT_USER_SETTINGS = """
            select user_id,
                   name,
                   phone,
                   referral_code,
                   user_type,
                   referrer_id,
                   source_1091617
            from bot_help_user_details
            where bot_help_user_id = :bot_help_user_id
            """;

    private static final String SQL_INSERT_BOT_USER_DETAILS = """
            insert into bot_help_user_details(name, phone, user_type, date_create, referrer_id, bot_help_user_id)
            values (:name, :phone, :user_type, :date_create, :referrer_id, :bot_help_user_id)
            """;

    private static final String SQL_INSERT_LEAD_INFO = """
            insert into lead_info(user_id, client_name, client_phone, date_create, request_id, comment)
            values (:user_id, :client_name, :client_phone, :date_create, :request_id, :comment)
            """;

    private static final String SQL_UPDATE_ORDER_ID = """
            update lead_info set order_id = :order_id where request_id = :request_id
            """;

    private static final String SQL_SELECT_REFERRERS = """
            select user_id, name, phone
            from bot_help_user_details
            where referrer_id = :user_id
            """;

    private static final String SQL_SELECT_ORDER_IDS_BY_USER_ID = """
            select order_id, client_name, client_phone from lead_info where user_id = :user_id
            """;

    public ApiBotRepository(JdbcTemplate template) {
        super(template);
    }

    public boolean isExistSessionByBotHelpUserId(String botHelpUserId) {
        MapSqlParameterSource params = new MapSqlParameterSource("bot_help_user_id", botHelpUserId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_SESSION, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistBotHelpUserId({}) with exception.", botHelpUserId, ex);
        }
        return false;
    }

    @Nullable
    public Long getUserIdByAccessToken(String accessToken) {
        MapSqlParameterSource params = new MapSqlParameterSource("access_token", accessToken);

        try {
            return template.queryForObject(SQL_SELECT_USER_ID_BY_ACCESS_TOKEN, params, Long.class);
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("invoke getUserIdByAccessToken({}) with exception.", accessToken, ex);
        }
        return null;
    }

    public boolean addUserSession(long userId, String botHelpUserId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("bot_help_user_id", botHelpUserId);

        try {
            return template.update(SQL_INSERT_BOT_USER_SESSION, params) > 0;
        } catch (DataAccessException ex) {
            logger.error("Invoke addUserSession({}, {}) with exception.", userId, botHelpUserId, ex);
        }
        return false;
    }

    @Nullable
    public Long getUserIdByReferralCode(String referralCode) {
        MapSqlParameterSource params = new MapSqlParameterSource("referral_code", referralCode);

        try {
            return template.queryForObject(SQL_SELECT_REFERER_ID_BY_CODE, params, Long.class);
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getUserIdByReferralCode({}) with exception.", referralCode, ex);
        }
        return null;
    }

    public boolean signUpReferral(long referrerId, String name, String phone, String botHelpUserId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("referrer_id", referrerId);
        params.addValue("user_type", BotUserDetails.UserType.REFERRER.name());
        params.addValue("name", name);
        params.addValue("phone", phone);
        params.addValue("date_create", Timestamp.valueOf(DateUtils.now()));
        params.addValue("bot_help_user_id", botHelpUserId);

        try {
            return template.update(SQL_INSERT_BOT_USER_DETAILS, params) > 0;
        } catch (DataAccessException ex) {
            logger.error("Invoke signUpReferral({}, {}, {}, {}) with exception.",
                    referrerId, name, phone, botHelpUserId, ex);
        }
        return false;
    }

    @Nullable
    public BotUserDetails getBotUserDetailsById(String botHelpUserId) {
        MapSqlParameterSource params = new MapSqlParameterSource("bot_help_user_id", botHelpUserId);
        try {
            return template.queryForObject(SQL_SELECT_BOT_USER_SETTINGS, params,
                    (rs, i) -> new BotUserDetails(
                            rs.getLong("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("referral_code"),
                            BotUserDetails.UserType.of(rs.getString("user_type")),
                            rs.getBoolean("source_1091617")
                    )
            );
        } catch (EmptyResultDataAccessException ignored) {
        } catch (DataAccessException ex) {
            logger.error("Invoke getBotUserDetailsById({}) with exception.", botHelpUserId, ex);
        }
        return null;
    }

    public void saveReferralLead(long userId, String clientName, String clientPhone, String comment, String requestId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("client_name", clientName);
        params.addValue("client_phone", clientPhone);
        params.addValue("request_id", requestId);
        params.addValue("comment", comment);
        params.addValue("date_create", Timestamp.valueOf(DateUtils.now()));

        try {
            template.update(SQL_INSERT_LEAD_INFO, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke saveLead({}, {}, {}, {}, {}) with exception.", userId, clientName, clientPhone, comment, requestId,
                    ex);
        }
    }

    public void saveOrderId(String requestId, long orderId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("request_id", requestId);
        params.addValue("order_id", orderId);

        try {
            template.update(SQL_UPDATE_ORDER_ID, params);
        } catch (DataAccessException ex) {
            logger.error("Invoke updateOrder({}, {}) with exception.", requestId, orderId, ex);
        }
    }

    public Collection<BotReferrer.Builder> getReferrersBuilders(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("user_id", userId);
        try {
            return template.query(SQL_SELECT_REFERRERS, params, (rs, i) ->
                    BotReferrer.Builder.getInstance()
                            .setUserId(rs.getLong("user_id"))
                            .setPhone(rs.getString("phone"))
                            .setName(rs.getString("name"))
            );
        } catch (DataAccessException ex) {
            logger.error("Invoke getReferrers({}) with exception.", userId, ex);
        }
        return Collections.emptyList();
    }

    public Collection<ReferralLead> getReferralLeads(long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("user_id", userId);
        try {
            return template.query(SQL_SELECT_ORDER_IDS_BY_USER_ID, params, (rs, i) -> new ReferralLead(
                    rs.getLong("order_id"),
                    rs.getString("client_name"),
                    rs.getString("client_phone")
            ));
        } catch (DataAccessException ex) {
            logger.error("Invoke getOrderIds({}) with exception.", userId, ex);
        }
        return Collections.emptySet();
    }

    public Collection<Pair<Long, Long>> leadIdToContactId(long statusId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", statusId);
        String sql = """
                select lead_id, contact_id
                from amo_crm_lead_contact
                where lead_id in (select lead_id
                                  from amo_crm_lead_info
                                  where option_source_id = :id)
                """;
        try {
            return template.query(sql, params, (rs, i) -> Pair.of(rs.getLong("lead_id"), rs.getLong("contact_id")));
        } catch (DataAccessException ex) {
            logger.error("Invoke leadIdToContactId({}) with exception.", statusId, ex);
        }
        return Collections.emptyList();
    }
}
