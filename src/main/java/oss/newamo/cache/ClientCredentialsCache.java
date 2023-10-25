package oss.newamo.cache;

import oss.newamo.annotation.Cache;
import oss.newamo.domain.credentials.ClientCredentials;
import oss.newamo.domain.event.RefreshClientCredentials;
import oss.newamo.repository.ClientConnectionRepository;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import static oss.backend.configuration.ThreadScopeConfiguration.DEFAULT_EXECUTOR;

@Cache
public class ClientCredentialsCache implements ApplicationListener<RefreshClientCredentials> {
    private static final Logger logger = LoggerFactory.getLogger(ClientCredentialsCache.class);

    private static final long CLIENT_CREDENTIALS_CACHE_EXPIRE_AFTER_WRITE = Long.getLong(
            "client.credentials.cache.expire.after.write.min", 10L
    );

    private final LoadingCache<String, Optional<ClientCredentials>> clientCredentialsCache;

    public ClientCredentialsCache(ClientConnectionRepository clientConnectionRepository,
            @Qualifier(DEFAULT_EXECUTOR) ExecutorService executor) {
        this.clientCredentialsCache = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(CLIENT_CREDENTIALS_CACHE_EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .build(clientId -> {
                    ClientCredentials credentials = clientConnectionRepository.getClientCredentials(clientId);
                    logger.debug("Found credentials: {} by clientId: {}.", credentials, clientId);
                    return Optional.ofNullable(credentials);
                });
    }

    public Optional<ClientCredentials> getClientCredentials(String clientId) {
        return clientCredentialsCache.get(clientId);
    }

    @Override
    public void onApplicationEvent(RefreshClientCredentials event) {
        String clientId = event.getClientId();
        logger.debug("Invoke clientCredentialsCache.invalidate({}).", clientId);
        clientCredentialsCache.invalidate(clientId);
    }
}
