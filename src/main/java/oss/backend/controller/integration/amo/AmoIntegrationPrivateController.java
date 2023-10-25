package oss.backend.controller.integration.amo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import oss.backend.domain.Profile;
import oss.backend.util.RequestUtils;
import oss.backend.util.SecurityUtils;
import oss.backend.util.SequenceUtils;
import oss.newamo.service.ClientConnectionService;

import java.net.URI;

@RestController
@RequestMapping(path = "${integration.private}" + AmoIntegration.AMO_PATH)
public class AmoIntegrationPrivateController extends AmoIntegration {
    private static final Logger logger = LoggerFactory.getLogger(AmoIntegrationPrivateController.class);

    private static final String AMO_API_PATH = "https://www.amocrm.ru/oauth";

    private static final String PARAM_STATE = "state";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_ORIGINAL = "original";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_REDIRECT_URI = "redirect_uri";
    private static final String PARAM_SECRETS_URI = "secrets_uri";
    private static final String PARAM_LOGO = "logo";
    private static final String PARAM_SCOPES = "scopes[]";

    private static final String AMO_INTEGRATION_NAME = "Order Stat Studio Connector";
    private static final String AMO_INTEGRATION_DESCRIPTION = "OSS Connector provide synchronization with amoCRM";
    private static final String AMO_INTEGRATION_MODE = "post_message";
    private static final String AMO_INTEGRATION_SCOPE_CRM = "crm";
    private static final String AMO_INTEGRATION_SCOPE_NOTIFICATIONS = "notifications";

    private static final String INITIAL_CONNECTION_RESPONSE_TEMPLATE = "{\"url\": \"%s\"}";
    private static final String CONNECTION_DETAILS_RESPONSE_TEMPLATE = "{\"connected\": %b}";

    private final String integrationPublicPath;
    private final ClientConnectionService clientConnectionService;

    public AmoIntegrationPrivateController(
            @Value("${integration.public}") String integrationPublicPath,
            ClientConnectionService clientConnectionService) {
        this.integrationPublicPath = integrationPublicPath;
        this.clientConnectionService = clientConnectionService;
    }

    @GetMapping("/connection-details")
    public ResponseEntity<String> getConnectionDetails() {
        Profile profile = SecurityUtils.getCurrentUser().getProfile();
        return ResponseEntity.ok(String.format(CONNECTION_DETAILS_RESPONSE_TEMPLATE,
                clientConnectionService.isExistConnection(profile.spaceId())
        ));
    }

    @GetMapping("/initial-connection")
    public ResponseEntity<String> initialConnection() {
        Profile profile = SecurityUtils.getCurrentUser().getProfile();

        String clientId = SequenceUtils.getCharactersToken(32);

        String original = RequestUtils.getCurrentPath();
        String integrationPath = original + integrationPublicPath + AMO_PATH;
        String redirectUri = integrationPath + REDIRECT_PATH;

        String uriString = UriComponentsBuilder.newInstance()
                .uri(URI.create(AMO_API_PATH))
                .queryParam(PARAM_STATE, clientId)
                .queryParam(PARAM_MODE, AMO_INTEGRATION_MODE)
                .queryParam(PARAM_ORIGINAL, original)
                .queryParam(PARAM_NAME, AMO_INTEGRATION_NAME)
                .queryParam(PARAM_DESCRIPTION, AMO_INTEGRATION_DESCRIPTION)
                .queryParam(PARAM_REDIRECT_URI, redirectUri)
                .queryParam(PARAM_SECRETS_URI, integrationPath + CONNECT_PATH)
                .queryParam(PARAM_LOGO, integrationPath + LOGO_PATH)
                .queryParam(PARAM_SCOPES, AMO_INTEGRATION_SCOPE_CRM)
                .queryParam(PARAM_SCOPES, AMO_INTEGRATION_SCOPE_NOTIFICATIONS)
                .build()
                .toUriString();

        logger.info("Prepared uri: {}.", uriString);
        logger.info("State: {}, profile: {}.", clientId, profile);
        clientConnectionService.initialConnectionSettings(clientId, profile.spaceId(), redirectUri);
        return ResponseEntity.ok(String.format(INITIAL_CONNECTION_RESPONSE_TEMPLATE, uriString));
    }
}
