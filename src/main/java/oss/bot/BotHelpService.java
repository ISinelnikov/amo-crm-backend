package oss.bot;

import oss.oldamo.domain.api.LeadModel;
import oss.oldamo.service.LeadService;
import oss.backend.util.CustomFieldUtils;
import oss.bot.repository.ApiBotRepository;
import oss.newamo.domain.contact.AmoCrmContact;
import oss.newamo.service.ContactService;
import oss.newamo.service.PipelineService;
import oss.newamo.service.UserService;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static oss.oldamo.service.LeadService.REJECT_REASON_TEXT_ID;

@Service
public class BotHelpService {
    private static final Logger logger = LoggerFactory.getLogger(ApiBotRepository.class);

    private final ApiBotRepository apiBotRepository;
    private final LeadService leadService;

    private final ContactService contactService;
    private final PipelineService pipelineService;
    private final UserService userService;

    public BotHelpService(ApiBotRepository apiBotRepository, LeadService leadService,
            ContactService contactService, PipelineService pipelineService, UserService userService) {
        this.apiBotRepository = apiBotRepository;
        this.leadService = leadService;

        this.contactService = contactService;
        this.pipelineService = pipelineService;
        this.userService = userService;
    }

    public boolean isExistSessionByBotHelpUserId(String botHelpUserId) {
        return apiBotRepository.isExistSessionByBotHelpUserId(botHelpUserId);
    }

    public boolean signIn(String accessToken, String botHelpUserId) {
        if (isExistSessionByBotHelpUserId(botHelpUserId)) {
            logger.debug("Invoke signIn({}, {}). Session already exist.", accessToken, botHelpUserId);
            return true;
        }
        Long userId = apiBotRepository.getUserIdByAccessToken(accessToken);
        logger.debug("Invoke logIn({}, {}), user id: {}.", accessToken, botHelpUserId, userId);
        if (userId != null) {
            return apiBotRepository.addUserSession(userId, botHelpUserId);
        }
        return false;
    }

    @Nullable
    public Long getUserIdByReferralCode(String referralCode) {
        return apiBotRepository.getUserIdByReferralCode(referralCode);
    }

    public boolean signUp(long referrerId, String name, String phone, String botHelpUserId) {
        return apiBotRepository.signUpReferral(referrerId, name, phone, botHelpUserId);
    }

    @Nullable
    public BotUserDetails getBotUserDetailsById(String botHelpUserId) {
        return apiBotRepository.getBotUserDetailsById(botHelpUserId);
    }

    public Collection<BotReferrer> getMyReferrerLeads(long userId) {
        return apiBotRepository.getReferrersBuilders(userId)
                .stream()
                .map(builder -> builder
                        .setExternalLeads(prepareExternalLeads(builder.getUserId()))
                        .build()
                )
                .toList();
    }

    public Collection<ExternalLead> getMyLeads(long userId) {
        return prepareExternalLeads(userId);
    }

    public Collection<ExternalLead> getByStatus(long statusId) {
        Collection<Pair<Long, Long>> pairs = apiBotRepository.leadIdToContactId(statusId);
        return pairs.stream().map(pair -> {
            Long key = pair.getKey();
            Long value = pair.getValue();

            if (key == null || value == null) {
                return null;
            }

            String uuid = "a734bcaf-419e-4bcb-a102-aad3557c3e70";

            AmoCrmContact contact = contactService.getAmoCrmContact(uuid, value);
            LeadModel lead = leadService.getLeadById(key).orElse(null);

            if (contact != null && lead != null) {
                Long updatedBy = lead.getUpdatedBy();
                oss.newamo.domain.user.User user = updatedBy == null ? null : userService.getUser(uuid, updatedBy);

                Long pipelineId = lead.getPipelineId();

                oss.newamo.domain.pipeline.status.PipelineStatus status = pipelineId != null
                        ? pipelineService.getPipelineStatus(uuid, pipelineId, statusId) : null;

                return new ExternalLead(
                        lead.getId(),
                        contact.getName(),
                        CustomFieldUtils.getFirstValue(contact.getCustomFields().stream().findFirst().orElse(null)),
                        lead.getName(),
                        user == null ? null : user.name(),
                        statusId,
                        status == null ? null : status.name(),
                        CustomFieldUtils.getFirstValue(lead.getFields().get(REJECT_REASON_TEXT_ID)),
                        lead.getCreatedDate(),
                        lead.getUpdatedDate()
                );
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }

    private Collection<ExternalLead> prepareExternalLeads(long userId) {
        return apiBotRepository.getReferralLeads(userId)
                .stream()
                .map(referralLead -> leadService.getLeadById(referralLead.leadId())
                        .map(lead -> {
                            String uuid = "a734bcaf-419e-4bcb-a102-aad3557c3e70";
                            Long updatedBy = lead.getUpdatedBy();
                            oss.newamo.domain.user.User user = updatedBy == null ? null : userService.getUser(uuid, updatedBy);

                            Long pipelineId = lead.getPipelineId();
                            Long statusId = lead.getStatusId();

                            oss.newamo.domain.pipeline.status.PipelineStatus status = (pipelineId != null && statusId != null)
                                    ? pipelineService.getPipelineStatus(uuid, pipelineId, statusId) : null;

                            return new ExternalLead(
                                    lead.getId(),
                                    referralLead.clientName(),
                                    referralLead.clientPhone(),
                                    lead.getName(),
                                    user == null ? null : user.name(),
                                    statusId,
                                    status == null ? null : status.name(),
                                    CustomFieldUtils.getFirstValue(lead.getFields().get(REJECT_REASON_TEXT_ID)),
                                    lead.getCreatedDate(),
                                    lead.getUpdatedDate());
                        }).orElse(null)
                )
                .toList();
    }

    public void processLeadInfo(
            String botHelpUserId, @Nullable String clientName,
            @Nullable String clientPhone, @Nullable String comment
    ) {
        String requestId = UUID.randomUUID().toString();
        BotUserDetails details = getBotUserDetailsById(botHelpUserId);
        if (details != null) {
            apiBotRepository.saveReferralLead(details.userId(), clientName, clientPhone, comment, requestId);
            leadService.sendLead(details.name(), details.phone(), clientName, clientPhone, comment, requestId,
                    response -> {
                        long leadId = response.id();
                        apiBotRepository.saveOrderId(requestId, leadId);
                        leadService.addLead("", leadId);
                    });
        }
    }
}
