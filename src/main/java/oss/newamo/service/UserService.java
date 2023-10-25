package oss.newamo.service;

import oss.newamo.cache.UserCache;
import oss.newamo.domain.event.AddedClientCredentials;
import oss.newamo.domain.user.AmoCrmUser;
import oss.newamo.domain.user.User;
import oss.newamo.integration.UserIntegration;
import oss.newamo.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class UserService implements ApplicationListener<AddedClientCredentials> {
    private final UserCache userCache;
    private final UserIntegration userIntegration;
    private final UserRepository userRepository;

    public UserService(UserCache userCache, UserIntegration userIntegration, UserRepository userRepository) {
        this.userCache = userCache;
        this.userIntegration = userIntegration;
        this.userRepository = userRepository;

        //onApplicationEvent(new AddedClientCredentials(this, "a734bcaf-419e-4bcb-a102-aad3557c3e70"));
    }

    public Collection<User> getUsers(String clientId) {
        return userCache.getUsers(clientId);
    }

    @Nullable
    public User getUser(String clientId, long userId) {
        return userCache.getUser(clientId, userId);
    }

    @Override
    public void onApplicationEvent(AddedClientCredentials event) {
        String clientId = event.getClientId();
        userIntegration.loadAmoCrmUsersAsync(
                clientId, amoCrmUsers -> {
                    List<AmoCrmUser> newUsers = amoCrmUsers
                            .stream()
                            .filter(amoCrmUser -> !userRepository.isExistUser(clientId, amoCrmUser.getId()))
                            .toList();
                    userRepository.saveUsers(clientId, newUsers);
                    userCache.invalidateUsersCache(clientId);
                }
        );
    }
}
