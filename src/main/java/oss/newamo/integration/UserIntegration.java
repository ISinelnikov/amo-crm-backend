package oss.newamo.integration;

import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Integration;
import oss.newamo.cache.ClientCredentialsCache;
import oss.newamo.domain.user.AmoCrmUser;

import java.util.Collection;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

@Integration
public class UserIntegration {
    private static final Logger logger = LoggerFactory.getLogger(UserIntegration.class);

    private static final String API_PATH_USERS = "/api/v4/users";

    private final ClientCredentialsCache clientCredentialsCache;

    public UserIntegration(ClientCredentialsCache clientCredentialsCache) {
        this.clientCredentialsCache = clientCredentialsCache;
    }

    public void loadAmoCrmUsersAsync(String clientId, Consumer<Collection<AmoCrmUser>> promise) {
        clientCredentialsCache.getClientCredentials(clientId).ifPresent(credentials -> {
            String preparedUrl = credentials.getAmoCrmPath() + API_PATH_USERS;
            HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

            ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

            logger.debug("Invoke url: {}, result code: {}, body: {}.", preparedUrl, response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                UserPage userPage = MappingUtils.parseJsonToInstance(response.getBody(), UserPage.class);
                if (userPage != null) {
                    promise.accept(userPage.result());
                }
            }
        });
    }

    public record UserPage(Embedded embedded) {
        @JsonCreator
        public UserPage(@JsonProperty("_embedded") Embedded embedded) {
            this.embedded = requireNonNull(embedded, "embedded can't be null.");
        }

        public record Embedded(Collection<AmoCrmUser> users) {
            @JsonCreator
            public Embedded(@JsonProperty("users") Collection<AmoCrmUser> users) {
                this.users = requireNonNull(users, "pipelines can't be null.");
            }
        }

        public Collection<AmoCrmUser> result() {
            return embedded().users();
        }
    }
}
