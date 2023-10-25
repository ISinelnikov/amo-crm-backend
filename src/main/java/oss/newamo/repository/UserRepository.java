package oss.newamo.repository;

import oss.newamo.domain.user.AmoCrmUser;
import oss.newamo.domain.user.User;

import java.util.Collection;

public interface UserRepository {
    boolean isExistUser(String clientId, long userId);

    Collection<User> getUsers(String clientId);

    void saveUsers(String clientId, Collection<AmoCrmUser> users);

    void updateUserVisible(String clientId, long userId, boolean hidden);
}
