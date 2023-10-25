package oss.newamo.integration;

import oss.newamo.domain.auth.OAuthRequest;
import oss.newamo.domain.auth.OAuthResponse;
import oss.backend.util.HttpUtils;
import oss.backend.util.MappingUtils;
import oss.newamo.annotation.Integration;
import oss.newamo.domain.credentials.InitialCredentials;
import oss.newamo.domain.credentials.ClientCredentials;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

@Integration
public class ClientConnectionIntegration {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionIntegration.class);

    private static final String OAUTH2_PATH = "/oauth2/access_token";

    public void authInitialCredentials(InitialCredentials credentials, Consumer<Optional<OAuthResponse>> promise) {
        promise.accept(Optional.ofNullable(
                response(OAuthRequest.authorizationCode(
                        credentials.getClientId(),
                        credentials.getClientSecret(),
                        credentials.getCode(),
                        credentials.getRedirectUri()),
                        credentials.getAmoCrmPath())));
    }

    public void refreshClientCredentials(ClientCredentials credentials, Consumer<Optional<OAuthResponse>> promise) {
        promise.accept(Optional.ofNullable(
                response(OAuthRequest.refreshToken(
                        credentials.getClientId(),
                        credentials.getAccessToken(),
                        credentials.getRefreshToken(),
                        credentials.getRedirectUri()),
                        credentials.getAmoCrmPath())
        ));
    }

    @Nullable
    private OAuthResponse response(OAuthRequest request, String amoCrmPath) {
        ResponseEntity<String> response = HttpUtils.jsonPostRequest(amoCrmPath + OAUTH2_PATH, HttpUtils.EMPTY_HEADERS, request);
        logger.debug("Invoke response({}) with result, code: {}, body: {}.", request, response.getStatusCode(), response.getBody());
        if (!response.getStatusCode().is2xxSuccessful()) {
            return null;
        }
        return MappingUtils.parseJsonToInstance(response.getBody(), OAuthResponse.class);
    }
}
