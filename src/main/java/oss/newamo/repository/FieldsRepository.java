package oss.newamo.repository;

import oss.newamo.domain.field.AmoCrmField;
import oss.newamo.domain.field.AmoCrmFieldEntity;
import oss.newamo.domain.field.AmoCrmFieldValue;

import java.util.Collection;

public interface FieldsRepository {

    boolean isExistCustomField(String clientId, long fieldId, AmoCrmFieldEntity entity);

    void addCustomFields(String clientId, Collection<AmoCrmField> fields, AmoCrmFieldEntity entity);

    boolean isExistCustomFieldValue(String clientId, long fieldId, long valueId);

    void addCustomLeadFieldValues(String clientId, long fieldId, Collection<AmoCrmFieldValue> values);
}
