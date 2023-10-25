package oss.newamo.repository.impl;

import oss.backend.repository.AbstractRepository;
import oss.newamo.domain.field.AmoCrmField;
import oss.newamo.domain.field.AmoCrmFieldEntity;
import oss.newamo.domain.field.AmoCrmFieldValue;
import oss.newamo.repository.FieldsRepository;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class PgFieldsRepository extends AbstractRepository implements FieldsRepository {
    private static final Logger logger = LoggerFactory.getLogger(PgFieldsRepository.class);

    private static final String SQL_SELECT_IS_EXIST_CUSTOM_FIELD = """
            select exists(select 1 from amo_crm_custom_field where client_id = :client_id and id = :field_id and entity = :entity) as is_exist
            """;

    private static final String SQL_INSERT_CUSTOM_FIELDS = """
            insert into amo_crm_custom_field (id, name, type, entity)
            values (:id, :name, :type, :entity)
            """;

    private static final String SQL_SELECT_IS_EXIST_CUSTOM_FIELD_VALUE = """
            select exists(select 1 from amo_crm_custom_field_value where client_id = :client_id and field_id = :field_id and id = :value_id) as is_exist
            """;

    private static final String SQL_INSERT_CUSTOM_FIELD_VALUES = """
            insert into amo_crm_custom_field_value (id, value, field_id)
            values (:id, :value, :field_id)
            """;

    public PgFieldsRepository(JdbcTemplate template) {
        super(template);
    }

    @Override
    public boolean isExistCustomField(String clientId, long fieldId, AmoCrmFieldEntity entity) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("field_id", fieldId);
        params.addValue("entity", entity.name());
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_CUSTOM_FIELD, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistCustomField({}, {}, {}) with exception.", clientId, fieldId, entity, ex);
        }
        return false;
    }

    @Override
    public void addCustomFields(String clientId, Collection<AmoCrmField> fields, AmoCrmFieldEntity entity) {
        MapSqlParameterSource[] params = fields
                .stream()
                .map(customField -> {
                    MapSqlParameterSource field = new MapSqlParameterSource();
                    field.addValue("id", customField.getId());
                    field.addValue("name", customField.getName());
                    field.addValue("type", customField.getType());
                    field.addValue("entity", entity.name());
                    return field;
                })
                .toArray(MapSqlParameterSource[]::new);
        try {
            template.batchUpdate(SQL_INSERT_CUSTOM_FIELDS, params);
        } catch (DataAccessException ex) {
            logger.info("Invoke addCustomFields({}, {}, {}) with exception.", clientId, fields, entity, ex);
        }
    }

    @Override
    public boolean isExistCustomFieldValue(String clientId, long fieldId, long valueId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("client_id", clientId);
        params.addValue("field_id", fieldId);
        params.addValue("value_id", valueId);
        try {
            return Boolean.TRUE.equals(template.queryForObject(SQL_SELECT_IS_EXIST_CUSTOM_FIELD_VALUE, params, Boolean.class));
        } catch (DataAccessException ex) {
            logger.error("Invoke isExistCustomFieldValue({}, {}, {}) with exception.", clientId, fieldId, valueId, ex);
        }
        return false;
    }

    @Override
    public void addCustomLeadFieldValues(String clientId, long fieldId, Collection<AmoCrmFieldValue> values) {
        MapSqlParameterSource[] params = values
                .stream()
                .map(value -> {
                    MapSqlParameterSource field = new MapSqlParameterSource();
                    field.addValue("id", value.getId());
                    field.addValue("value", value.getValue());
                    field.addValue("field_id", fieldId);
                    return field;
                })
                .toArray(MapSqlParameterSource[]::new);

        try {
            template.batchUpdate(SQL_INSERT_CUSTOM_FIELD_VALUES, params);
        } catch (DataAccessException ex) {
            logger.info("Invoke addCustomFieldValues({}, {}, {}) with exception.", clientId, fieldId, values, ex);
        }
    }
}
