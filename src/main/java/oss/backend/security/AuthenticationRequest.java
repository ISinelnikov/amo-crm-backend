package oss.backend.security;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record AuthenticationRequest(
        String requestId, ZonedDateTime dateCreate, long profileId, String username, String password,
        TwoFactorAuthType authType, @Nullable Integer twoFactorCode) {
    public AuthenticationRequest(String requestId, ZonedDateTime dateCreate,
            long profileId, String username, String password, TwoFactorAuthType authType, @Nullable Integer twoFactorCode) {
        this.requestId = requireNonNull(requestId, "requestId can't be null");
        this.dateCreate = requireNonNull(dateCreate, "dateCreate can't be null");
        this.profileId = profileId;
        this.username = requireNonNull(username, "username can't be null");
        this.password = requireNonNull(password, "password can't be null");
        this.authType = requireNonNull(authType, "authType can't be null");
        this.twoFactorCode = twoFactorCode;
    }
}
