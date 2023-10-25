package oss.newamo.cache;

import oss.newamo.annotation.Cache;
import oss.newamo.domain.user.User;
import oss.newamo.repository.UserRepository;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import static oss.backend.configuration.ThreadScopeConfiguration.DEFAULT_EXECUTOR;

@Cache
public class UserCache {
    private static final Logger logger = LoggerFactory.getLogger(PipelineCache.class);

    private static final long USER_CACHE_EXPIRE_AFTER_WRITE = Long.getLong("user.cache.expire.after.write.min", 10L);

    private final LoadingCache<String, Collection<User>> usersCache;

    public UserCache(UserRepository userRepository, @Qualifier(DEFAULT_EXECUTOR) ExecutorService executor) {
        this.usersCache = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(USER_CACHE_EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .build(clientId -> {
                    Collection<User> users = userRepository.getUsers(clientId);
                    logger.debug("Load users: {} by clientId: {}.", users, clientId);
                    return users;
                });
    }

    public Collection<User> getUsers(String clientId) {
        return usersCache.get(clientId);
    }

    @Nullable
    public User getUser(String clientId, long userId) {
        return usersCache.get(clientId)
                .stream()
                .filter(user -> user.id() == userId)
                .findFirst()
                .orElse(null);
    }

    public void invalidateUsersCache(String clientId) {
        usersCache.invalidate(clientId);
    }
}
