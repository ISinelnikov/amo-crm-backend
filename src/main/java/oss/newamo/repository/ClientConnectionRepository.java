package oss.newamo.repository;

import oss.newamo.domain.credentials.ClientCredentials;
import oss.newamo.domain.credentials.InitialCredentials;

import java.util.Collection;
import javax.annotation.Nullable;

public interface ClientConnectionRepository {
    boolean isExistClientConnection(long spaceId);

    void initialClientConnection(String operationId, long spaceId, String redirectUri);

    void updateClientConnectionSecret(String operationId, String clientId, String clientSecret);

    void updateClientConnectionCode(String operationId, String code, String referer, String clientId);

    @Nullable
    InitialCredentials getInitialCredentials(String clientId);

    @Nullable
    ClientCredentials getClientCredentials(String clientId);

    void refreshTokens(String clientId, String accessToken, String refreshToken);

    Collection<ClientCredentials> getAllRefreshableClientCredentials(long hours);
}
