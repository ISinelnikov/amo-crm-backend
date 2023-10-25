package oss.oldamo.service;

import org.springframework.stereotype.Service;
import oss.oldamo.domain.api.CreateContactRequest;
import oss.oldamo.domain.api.CreateLeadRequest;
import oss.oldamo.domain.api.CreateLeadResponse;
import oss.oldamo.domain.api.EmbeddedType;
import oss.oldamo.domain.api.LeadModel;
import oss.oldamo.domain.api.StatusDetailsDto;
import oss.oldamo.domain.api.UpdateLeadRequest;
import oss.oldamo.domain.api.common.CustomField;
import oss.oldamo.domain.api.common.CustomFieldValue;
import oss.oldamo.repository.LeadRepository;
import oss.backend.util.CustomFieldUtils;
import oss.backend.util.NumberUtils;
import oss.backend.util.OSSStringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class LeadService {
    private static final long CONTACT_ENUM = 38422;
    private static final long CONTACT_MOB_ID = 91098;

    public static final long SOURCE_SELECT_ID = 526649;
    public static final long COMMENT_FIELD_ID = 526663;
    public static final long REJECT_REASON_TEXT_ID = 526661;

    private static final long REVENUE_ID = 526931;

    private static final String BOT_NAME_TEMPLATE = "Рекомендация от партнера %s";

    private final AmoCrmRequestProcessor amoCrmRequestProcessor;
    private final LeadRepository leadRepository;

    public LeadService(AmoCrmRequestProcessor amoCrmRequestProcessor, LeadRepository leadRepository) {
        this.amoCrmRequestProcessor = amoCrmRequestProcessor;
        this.leadRepository = leadRepository;
    }

    public void sendLead(String name, String phone, @Nullable String clientName, String clientPhone,
            @Nullable String comment, String requestId, Consumer<CreateLeadResponse> response) {
        String systemComment = "Рекомендовал " + OSSStringUtils.valueToEmpty(name)
                + ", " + OSSStringUtils.valueToEmpty(phone) + ", " + OSSStringUtils.valueToEmpty(comment);

        CreateLeadRequest request = prepareLeadRequest(String.format(BOT_NAME_TEMPLATE, name), clientName,
                clientPhone, systemComment, requestId);
        amoCrmRequestProcessor.createLead("a734bcaf-419e-4bcb-a102-aad3557c3e70", request, response);
    }

    private static CreateLeadRequest prepareLeadRequest(String name, @Nullable String clientName, String clientPhone,
            String comment, String requestId) {
        CreateContactRequest contactRequest = new CreateContactRequest(clientName,
                Set.of(
                        CustomField.of(CONTACT_ENUM, CustomFieldValue.of(CONTACT_MOB_ID, clientPhone)),
                        CustomField.of(COMMENT_FIELD_ID, CustomFieldValue.of(comment))
                )
        );

        Collection<CustomField> fields = List.of(
                CustomField.of(SOURCE_SELECT_ID, CustomFieldValue.of(1090911))
        );

        return CreateLeadRequest.Builder.getInstance()
                .setName(name)
                .setPipelineStatusId(null)
                .setCustomFields(fields)
                .addEmbeddedValue(EmbeddedType.CONTACTS, contactRequest)
                .setRequestId(requestId)
                .build();
    }

    public void addLead(String clientId, long leadId) {
        if (!leadRepository.isExistLead(leadId)) {
            LeadModel leadDto = amoCrmRequestProcessor.getAmoCrmLeadDto(clientId, leadId);

            if (leadDto == null) {
                return;
            }

            Map<Long, CustomField> idToCustomField = leadDto.getFields();

            Long sourceId = CustomFieldUtils.getFirstValueId(idToCustomField.get(SOURCE_SELECT_ID));
            Long revenue = NumberUtils.longOrNull(CustomFieldUtils.getFirstValue(idToCustomField.get(REVENUE_ID)));

            String rejectReason = CustomFieldUtils.getFirstValue(idToCustomField.get(REJECT_REASON_TEXT_ID));
            String link = "/leads/detail/" + leadId;

            leadRepository.addLead(leadDto, link, sourceId == null ? null : SOURCE_SELECT_ID, sourceId, rejectReason, revenue);
            updateContacts(leadDto.getId(), leadDto.getContacts());
            synchronizeLeadStatusEvents(leadId);
        }
    }

    public Optional<LeadModel> getLeadById(long leadId) {
        return Optional.ofNullable(amoCrmRequestProcessor.getAmoCrmLeadDto("a734bcaf-419e-4bcb-a102-aad3557c3e70", leadId));
    }

    public void updateLead(String clientId, long leadId) {
        if (leadRepository.isExistLead(leadId)) {
            LeadModel leadDto = amoCrmRequestProcessor.getAmoCrmLeadDto(clientId, leadId);

            if (leadDto == null) {
                return;
            }

            Long sourceId = CustomFieldUtils.getFirstValueId(leadDto.getFields().get(SOURCE_SELECT_ID));
            leadRepository.updateLead(leadDto, sourceId);

            synchronizeLeadStatusEvents(leadId);
        } else {
            addLead(clientId, leadId);
        }
    }

    public void deleteLead(long leadId) {
        leadRepository.deleteLead(leadId);
    }

    private void synchronizeLeadStatusEvents(long leadId) {
        amoCrmRequestProcessor.getLeadStatusEventsDetails("a734bcaf-419e-4bcb-a102-aad3557c3e70", leadId, consumer -> {
            if (consumer == null) {
                return;
            }
            consumer.embedded().events().forEach(leadEventDto -> {
                String id = leadEventDto.getId();
                if (!leadRepository.isExistLeadEvent(id)) {
                    StatusDetailsDto.LeadStatus before = leadEventDto
                            .getBefore()
                            .stream()
                            .findFirst()
                            .map(StatusDetailsDto.LeadStatusWrapper::leadStatus)
                            .orElse(null);

                    StatusDetailsDto.LeadStatus after = leadEventDto
                            .getAfter()
                            .stream()
                            .findFirst()
                            .map(StatusDetailsDto.LeadStatusWrapper::leadStatus)
                            .orElse(null);

                    leadRepository.addLeadEvent(
                            id,
                            leadEventDto.getType(),
                            leadEventDto.getLeadId(),
                            leadEventDto.getUserId(),
                            leadEventDto.getDateCreate(),
                            before == null ? null : before.pipelineId(),
                            after == null ? null : after.pipelineId(),
                            before == null ? null : before.id(),
                            after == null ? null : after.id()
                    );
                }
            });
        });
    }

    private void updateContacts(long leadId, Collection<LeadModel.LeadEmbeddedContact> contacts) {
        Set<Long> existContacts = leadRepository.getLeadContactsIds(leadId);
        List<LeadModel.LeadEmbeddedContact> newContacts = contacts.stream()
                .filter(contact -> !existContacts.contains(contact.id()))
                .toList();
        if (!newContacts.isEmpty()) {
            leadRepository.addContacts(leadId, newContacts);
        }
    }

    public void changeLeadSourceId(long orderId, long optionId, long selectId) {
        amoCrmRequestProcessor.updateLead("a734bcaf-419e-4bcb-a102-aad3557c3e70", orderId,
                UpdateLeadRequest.of(CustomField.of(selectId, CustomFieldValue.of(optionId)))
        );
        leadRepository.updateLeadSourceId(orderId, optionId, selectId);
    }
}
