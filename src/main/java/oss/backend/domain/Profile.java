package oss.backend.domain;

import oss.backend.security.TwoFactorAuthType;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.Objects.requireNonNull;

public record Profile(
        long id, long spaceId, String username, @JsonIgnore String password,
        String firstName, String lastName, @Nullable String avatar,
        TwoFactorAuthType authType, @Nullable ZonedDateTime emailConfirmationDate
        ) implements Serializable {
    public Profile(
            long id, long spaceId, String username, String password,
            String firstName, String lastName, @Nullable String avatar,
            TwoFactorAuthType authType, @Nullable ZonedDateTime emailConfirmationDate
    ) {
        this.id = id;
        this.spaceId = spaceId;
        this.username = requireNonNull(username, "username can't be null.");
        this.password = requireNonNull(password, "password can't be null.");
        this.firstName = requireNonNull(firstName, "firstName can't be null.");
        this.lastName = requireNonNull(lastName, "lastName can't be null.");
        this.avatar = avatar;
        this.authType = requireNonNull(authType, "authType can't be null.");
        this.emailConfirmationDate = emailConfirmationDate;
    }
}
