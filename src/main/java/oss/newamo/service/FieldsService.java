package oss.newamo.service;

import oss.newamo.domain.event.AddedClientCredentials;
import oss.newamo.domain.field.AmoCrmField;
import oss.newamo.domain.field.AmoCrmFieldEntity;
import oss.newamo.domain.field.AmoCrmFieldValue;
import oss.newamo.integration.FieldsIntegration;
import oss.newamo.repository.FieldsRepository;

import java.util.List;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class FieldsService implements ApplicationListener<AddedClientCredentials> {
    private final FieldsIntegration fieldsIntegration;
    private final FieldsRepository fieldsRepository;

    public FieldsService(FieldsIntegration fieldsIntegration, FieldsRepository fieldsRepository) {
        this.fieldsIntegration = fieldsIntegration;
        this.fieldsRepository = fieldsRepository;
    }

    @Override
    public void onApplicationEvent(AddedClientCredentials event) {
        String clientId = event.getClientId();
        fieldsIntegration.loadAmoCrmLeadFieldsAsync(
                clientId, amoCrmLeadFields -> {
                    List<AmoCrmField> amoCrmFields = amoCrmLeadFields
                            .stream()
                            .filter(
                                    amoCrmField -> !fieldsRepository.isExistCustomField(clientId, amoCrmField.getId(),
                                            AmoCrmFieldEntity.LEAD)
                            )
                            .toList();
                    fieldsRepository.addCustomFields(clientId, amoCrmFields, AmoCrmFieldEntity.LEAD);

                    amoCrmFields.forEach(field -> {
                        if (field.getValues() != null) {
                            List<AmoCrmFieldValue> amoCrmFieldValues = field.getValues()
                                    .stream()
                                    .filter(
                                            amoCrmFieldValue -> !fieldsRepository.isExistCustomFieldValue(clientId,
                                                    field.getId(), amoCrmFieldValue.getId())
                                    )
                                    .toList();
                            fieldsRepository.addCustomLeadFieldValues(clientId, field.getId(), amoCrmFieldValues);
                        }
                    });
                }
        );
    }
}
