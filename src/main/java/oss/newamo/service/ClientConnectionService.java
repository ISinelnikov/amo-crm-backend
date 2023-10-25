package oss.newamo.service;

import oss.backend.configuration.ThreadScopeConfiguration;
import oss.newamo.domain.credentials.ClientCredentials;
import oss.newamo.domain.credentials.InitialCredentials;
import oss.newamo.domain.event.AddedClientCredentials;
import oss.newamo.domain.event.RefreshClientCredentials;
import oss.newamo.integration.ClientConnectionIntegration;
import oss.newamo.repository.ClientConnectionRepository;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ClientConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionService.class);

    private static final long CLIENT_CONNECTION_SERVICE_TOKEN_REFRESH_TIME = Long.getLong(
            "client.connection.service.token.refresh.time.hours", 3L
    );

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ClientConnectionIntegration clientConnectionIntegration;
    private final ClientConnectionRepository clientConnectionRepository;


    public ClientConnectionService(ApplicationEventPublisher applicationEventPublisher,
            ClientConnectionIntegration clientConnectionIntegration, ClientConnectionRepository clientConnectionRepository,
            @Qualifier(ThreadScopeConfiguration.DEFAULT_SCHEDULER) ScheduledExecutorService scheduledExecutorService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.clientConnectionIntegration = clientConnectionIntegration;
        this.clientConnectionRepository = clientConnectionRepository;

        scheduledExecutorService.scheduleWithFixedDelay(
                () -> clientConnectionRepository.getAllRefreshableClientCredentials(CLIENT_CONNECTION_SERVICE_TOKEN_REFRESH_TIME)
                        .forEach(this::refreshCredentials),
                0,
                CLIENT_CONNECTION_SERVICE_TOKEN_REFRESH_TIME,
                TimeUnit.HOURS
        );
    }

    public boolean isExistConnection(long spaceId) {
        return clientConnectionRepository.isExistClientConnection(spaceId);
    }

    public void initialConnectionSettings(String operationId, long spaceId, String redirectUri) {
        clientConnectionRepository.initialClientConnection(operationId, spaceId, redirectUri);
    }

    public void updateClientConnectionSecret(String operationId, String clientId, String clientSecret) {
        clientConnectionRepository.updateClientConnectionSecret(operationId, clientId, clientSecret);
        initialConnection(clientId);
    }

    public void updateClientConnectionCode(String operationId, String code, String referer, String clientId) {
        clientConnectionRepository.updateClientConnectionCode(operationId, code, referer, clientId);
        initialConnection(clientId);
    }

    private void initialConnection(String clientId) {
        InitialCredentials credentials = clientConnectionRepository.getInitialCredentials(clientId);
        if (credentials == null) {
            logger.info("Invoke initialConnection({}), credentials not found.", clientId);
            return;
        }
        clientConnectionIntegration.authInitialCredentials(credentials, result -> result.ifPresent(response -> {
            logger.info("Initial connection response: {} by credentials: {}.", response, credentials);
            clientConnectionRepository.refreshTokens(clientId, response.accessToken(), response.refreshToken());
            applicationEventPublisher.publishEvent(new AddedClientCredentials(this, clientId));
        }));
    }

    private void refreshCredentials(ClientCredentials credentials) {
        clientConnectionIntegration.refreshClientCredentials(credentials, result -> result.ifPresent(response -> {
            logger.info("Refresh credentials response: {} by credentials: {}.", response, credentials);
            clientConnectionRepository.refreshTokens(credentials.getClientId(), response.accessToken(), response.refreshToken());
            applicationEventPublisher.publishEvent(new RefreshClientCredentials(this, credentials.getClientId()));
        }));
    }
}
