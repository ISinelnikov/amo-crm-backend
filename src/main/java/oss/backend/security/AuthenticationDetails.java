package oss.backend.security;

import javax.annotation.Nullable;

public record AuthenticationDetails(
        @Nullable String token, @Nullable TwoFactorAuthType authType, @Nullable String twoFactorRequestId)
{
    public static AuthenticationDetails token(String token) {
        return new AuthenticationDetails(token, null, null);
    }

    public static AuthenticationDetails details(TwoFactorAuthType authType, String twoFactorRequestId) {
        return new AuthenticationDetails(null, authType, twoFactorRequestId);
    }
}
