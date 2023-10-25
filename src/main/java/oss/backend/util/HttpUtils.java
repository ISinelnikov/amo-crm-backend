package oss.backend.util;

import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    public static final HttpHeaders EMPTY_HEADERS;

    static {
        EMPTY_HEADERS = new HttpHeaders();
        EMPTY_HEADERS.setContentType(MediaType.APPLICATION_JSON);
    }

    private HttpUtils() {
    }

    public static ResponseEntity<String> jsonGetRequest(String requestPath, HttpHeaders headers) {
        return executeHttpRequest(HttpMethod.GET, requestPath, headers, null);
    }

    public static ResponseEntity<String> jsonPatchRequest(String requestPath, HttpHeaders headers, Object requestBody) {
        return executeHttpRequest(HttpMethod.PATCH, requestPath, headers, requestBody);
    }

    public static ResponseEntity<String> jsonPostRequest(String requestPath, HttpHeaders headers, Object requestBody) {
        return executeHttpRequest(HttpMethod.POST, requestPath, headers, requestBody);
    }

    private static ResponseEntity<String> executeHttpRequest(HttpMethod httpMethod, String requestPath,
            HttpHeaders headers, @Nullable Object message) {
        HttpEntity<?> request = new HttpEntity<>(message == null ? null : MappingUtils.convertObjectToJson(message), headers);
        try {
            return createTemplate().exchange(requestPath, httpMethod, request, String.class);
        } catch (HttpStatusCodeException ex) {
            logger.error("Request to {} failed with status: {} and message: {}.", requestPath, ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            return new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getStatusCode());
        } catch (RestClientException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static HttpHeaders getBearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", String.format("Bearer %s", token));
        return headers;
    }

    public static HttpHeaders addApiKeyHeaders(HttpHeaders headers, String apiKey) {
        headers.add("Api-key", apiKey);
        return headers;
    }

    public static HttpHeaders getApiKeyHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Api-key", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static RestTemplate createTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(requestFactory);
        return template;
    }
}
