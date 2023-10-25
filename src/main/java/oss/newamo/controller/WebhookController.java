package oss.newamo.controller;

import oss.oldamo.service.LeadService;
import oss.backend.util.WebhookUtils;
import oss.newamo.cache.ClientCredentialsCache;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/amo-crm/webhook")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final ClientCredentialsCache clientCredentialsCache;
    private final LeadService leadService;

    public WebhookController(ClientCredentialsCache clientCredentialsCache, LeadService leadService) {
        this.clientCredentialsCache = clientCredentialsCache;
        this.leadService = leadService;
    }

    @PostMapping(path = "/create-lead", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Map<String, Object>> processWebhook(@RequestParam MultiValueMap<String, String> webhook) {
        return processLeadWebhook(webhook, "a734bcaf-419e-4bcb-a102-aad3557c3e70");
    }

    @PostMapping(path = "/lead/{clientId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Map<String, Object>> processLeadWebhook(@RequestParam MultiValueMap<String, String> webhook,
            @PathVariable String clientId) {
        logger.info("Invoke processLeadWebhook(..., {}).", clientId);
        (StringUtils.hasText(clientId) ? clientCredentialsCache.getClientCredentials(clientId) : Optional.empty())
                .ifPresent(credentials -> {
                    Set<String> keys = webhook.keySet();

                    Set<String> added = keys.stream()
                            .filter(key -> WebhookUtils.isLeadId(WebhookUtils.LEAD_PREFIX, WebhookUtils.ADD_ACTION, key))
                            .collect(Collectors.toSet());

                    Set<String> updated = keys.stream()
                            .filter(key -> WebhookUtils.isLeadId(WebhookUtils.LEAD_PREFIX, WebhookUtils.UPDATE_ACTION, key))
                            .collect(Collectors.toSet());

                    Set<String> deleted = keys.stream()
                            .filter(key -> WebhookUtils.isLeadId(WebhookUtils.LEAD_PREFIX, WebhookUtils.DELETE_ACTION, key))
                            .collect(Collectors.toSet());

                    Set<Long> addedIds = added.stream()
                            .flatMap(addedKey -> webhook.get(addedKey).stream())
                            .map(WebhookUtils::parseLeadId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    Set<Long> updatedIds = updated.stream()
                            .flatMap(updatedKey -> webhook.get(updatedKey).stream())
                            .map(WebhookUtils::parseLeadId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    Set<Long> deletedIds = deleted.stream()
                            .flatMap(deletedKey -> webhook.get(deletedKey).stream())
                            .map(WebhookUtils::parseLeadId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    if (!addedIds.isEmpty()) {
                        logger.info("Invoke processLeadWebhook, clientId: {}, addedIds: {}.", clientId, addedIds);
                        addedIds.forEach(id -> leadService.addLead(clientId, id));
                    }
                    if (!updatedIds.isEmpty()) {
                        logger.info("Invoke processLeadWebhook, clientId: {}, updatedIds: {}.", clientId, updatedIds);
                        updatedIds.forEach(id -> leadService.updateLead(clientId, id));
                    }
                    if (!deletedIds.isEmpty()) {
                        logger.info("Invoke processLeadWebhook, clientId: {}, deletedIds: {}.", clientId, deletedIds);
                        deletedIds.forEach(id -> leadService.deleteLead(id));
                    }
                });

        return ResponseEntity.ok(Collections.emptyMap());
    }
}
