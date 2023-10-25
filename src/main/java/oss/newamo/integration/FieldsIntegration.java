package oss.newamo.integration;

import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Integration;
import oss.newamo.cache.ClientCredentialsCache;
import oss.newamo.domain.field.AmoCrmField;

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
public class FieldsIntegration {
    private static final Logger logger = LoggerFactory.getLogger(FieldsIntegration.class);

    private static final String API_LEAD_FIELDS_PATH = "/api/v4/leads/custom_fields?page=1&limit=100";

    private final ClientCredentialsCache clientCredentialsCache;

    public FieldsIntegration(ClientCredentialsCache clientCredentialsCache) {
        this.clientCredentialsCache = clientCredentialsCache;
    }

    public void loadAmoCrmLeadFieldsAsync(String clientId, Consumer<Collection<AmoCrmField>> promise) {
        clientCredentialsCache.getClientCredentials(clientId).ifPresent(credentials -> {
            String preparedUrl = credentials.getAmoCrmPath() + API_LEAD_FIELDS_PATH;
            HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

            ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

            logger.debug("Invoke url: {}, result code: {}, body: {}.", preparedUrl, response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                FieldsPage fieldsPage = MappingUtils.parseJsonToInstance(response.getBody(), FieldsPage.class);
                if (fieldsPage != null) {
                    promise.accept(fieldsPage.result());
                }
            }
        });
    }

    public record FieldsPage(Embedded embedded) {
        @JsonCreator
        public FieldsPage(@JsonProperty("_embedded") Embedded embedded) {
            this.embedded = requireNonNull(embedded, "embedded can't be null.");
        }

        public record Embedded(Collection<AmoCrmField> fields) {
            @JsonCreator
            public Embedded(@JsonProperty("custom_fields") Collection<AmoCrmField> fields) {
                this.fields = requireNonNull(fields, "Fields can't be null.");
            }
        }

        public Collection<AmoCrmField> result() {
            return embedded().fields();
        }
    }
}
