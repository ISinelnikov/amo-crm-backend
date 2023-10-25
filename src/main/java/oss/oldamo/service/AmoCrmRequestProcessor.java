package oss.oldamo.service;

import oss.oldamo.domain.api.CreateLeadRequest;
import oss.oldamo.domain.api.CreateLeadResponse;
import oss.oldamo.domain.api.LeadModel;
import oss.oldamo.domain.api.StatusDetailsDto;
import oss.oldamo.domain.api.UpdateLeadRequest;
import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.cache.ClientCredentialsCache;
import oss.newamo.domain.credentials.ClientCredentials;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AmoCrmRequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AmoCrmRequestProcessor.class);

    private static final String API_LEAD_COMPLEX_PATH = "/api/v4/leads/complex";

    private static final String API_LEAD_PATH = "/api/v4/leads/";

    private static final String WITH_CONTACTS = "?with=contacts";

    private static final String API_LEAD_STATUS_DETAILS = """
            /api/v4/events?filter[type]=lead_status_changed&filter[entity]=lead&filter[entity_id][]=
            """;

    private final AtomicLong counter = new AtomicLong(0);

    private final ClientCredentialsCache clientCredentialsCache;

    public AmoCrmRequestProcessor(ClientCredentialsCache clientCredentialsCache) {
        this.clientCredentialsCache = clientCredentialsCache;
    }

    public void createLead(String clientId, CreateLeadRequest request, Consumer<CreateLeadResponse> callback) {
        executeMulti(clientId, API_LEAD_COMPLEX_PATH, Set.of(request), CreateLeadResponse.class, callback);
    }

    public void getLeadStatusEventsDetails(String clientId, long leadId, Consumer<StatusDetailsDto> callback) {
        executeGetSingle(clientId, API_LEAD_STATUS_DETAILS + leadId, StatusDetailsDto.class, callback);
    }

    @Nullable
    public LeadModel getAmoCrmLeadDto(String clientId, long leadId) {
        ClientCredentials credentials = clientCredentialsCache.getClientCredentials(clientId).orElse(null);
        if (credentials == null) {
            return null;
        }
        String preparedUrl = credentials.getAmoCrmPath() + API_LEAD_PATH + leadId + WITH_CONTACTS;
        ResponseEntity<String> response = HttpUtils.jsonGetRequest(
                preparedUrl, HttpUtils.getBearerHeaders(credentials.getAccessToken())
        );

        logger.debug("Invoke url: {}, result countryCode: {}, result body: {}.", preparedUrl,
                response.getStatusCode(), response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            return MappingUtils.parseJsonToInstance(response.getBody(), LeadModel.class);
        }
        return null;
    }

    public void updateLead(String clientId, long orderId, UpdateLeadRequest request) {
        ClientCredentials credentials = clientCredentialsCache.getClientCredentials(clientId).orElse(null);
        if (credentials == null) {
            return;
        }
        String preparedUrl = credentials.getAmoCrmPath() + "/api/v4/leads/" + orderId;
        ResponseEntity<String> response = HttpUtils.jsonPatchRequest(
                preparedUrl, HttpUtils.getBearerHeaders(credentials.getAccessToken()), request
        );
        logger.info("Invoke updateLead({}), result: {}", request, response.getBody());
    }

    private <R, T> void executeMulti(String clientId, String apiPath, @Nullable Collection<R> request, Class<T> result, Consumer<T> callback) {
        ClientCredentials credentials = clientCredentialsCache.getClientCredentials(clientId).orElse(null);
        if (credentials == null) {
            return;
        }
        String preparedUrl = credentials.getAmoCrmPath() + apiPath;
        HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

        ResponseEntity<String> response = request != null
                ? HttpUtils.jsonPostRequest(preparedUrl, headers, request)
                : HttpUtils.jsonGetRequest(preparedUrl, headers);

//        logger.debug("Invoke url: {}, request: {}, result countryCode: {}, result body: {}.", preparedUrl, request,
//                response.getStatusCode(), response.getBody());
        if (response.getStatusCode().is2xxSuccessful()) {
            MappingUtils.parseJsonToCollection(response.getBody(), result).forEach(callback);
        }
        long value = counter.incrementAndGet();
        if (value % 7 == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private <R, T> void executeGetSingle(String clientId, String apiPath, Class<T> result, Consumer<T> callback) {
        ClientCredentials credentials = clientCredentialsCache.getClientCredentials(clientId).orElse(null);
        if (credentials == null) {
            return;
        }
        String preparedUrl = credentials.getAmoCrmPath() + apiPath;
        HttpHeaders headers = HttpUtils.getBearerHeaders(credentials.getAccessToken());

        ResponseEntity<String> response = HttpUtils.jsonGetRequest(preparedUrl, headers);

//        logger.debug("Invoke url: {}, request: {}, result countryCode: {}, result body: {}.", preparedUrl, request,
//                response.getStatusCode(), response.getBody());
        if (response.getStatusCode().is2xxSuccessful()) {
            callback.accept(MappingUtils.parseJsonToInstance(response.getBody(), result));
        }
        long value = counter.incrementAndGet();
        if (value % 7 == 0) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
