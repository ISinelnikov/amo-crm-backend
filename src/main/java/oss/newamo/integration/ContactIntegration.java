package oss.newamo.integration;

import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Integration;
import oss.newamo.cache.ClientCredentialsCache;
import oss.newamo.domain.contact.AmoCrmContact;
import oss.newamo.domain.credentials.ClientCredentials;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Integration
public class ContactIntegration {
    private static final Logger logger = LoggerFactory.getLogger(UserIntegration.class);

    private static final String API_PATH_USERS = "/api/v4/contacts/{contact_id}";

    private final ClientCredentialsCache clientCredentialsCache;

    public ContactIntegration(ClientCredentialsCache clientCredentialsCache) {
        this.clientCredentialsCache = clientCredentialsCache;
    }

    @Nullable
    public AmoCrmContact loadAmoCrmContactById(String clientId, long contactId) {
        ClientCredentials credentials = clientCredentialsCache.getClientCredentials(clientId).orElse(null);
        if (credentials == null) {
            return null;
        }
        String preparedUrl = credentials.getAmoCrmPath()
                + API_PATH_USERS.replace("{contact_id}", String.valueOf(contactId));
        HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

        ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

        logger.debug("Invoke url: {}, result code: {}, body: {}.", preparedUrl, response.getStatusCode(), response.getBody());
        if (response.getStatusCode().is2xxSuccessful()) {
            return MappingUtils.parseJsonToInstance(response.getBody(), AmoCrmContact.class);
        }
        return null;
    }
}
