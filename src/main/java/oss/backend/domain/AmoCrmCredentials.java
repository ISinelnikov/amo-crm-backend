package oss.backend.domain;

import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;

public record AmoCrmCredentials(String clientId, String clientSecret,
                                String accessToken, String refreshToken,
                                ZonedDateTime lastRefreshTokenDateCreate, ZonedDateTime dateCreate) {
    public AmoCrmCredentials(String clientId, String clientSecret,
            String accessToken, String refreshToken, ZonedDateTime lastRefreshTokenDateCreate,
            ZonedDateTime dateCreate) {
        this.clientId = requireNonNull(clientId, "Client Id can't be null.");
        this.clientSecret = requireNonNull(clientSecret, "Client Secret can't be null.");
        this.accessToken = requireNonNull(accessToken, "Access Token can't be null.");
        this.refreshToken = requireNonNull(refreshToken, "Refresh Token can't be null.");
        this.lastRefreshTokenDateCreate = requireNonNull(
                lastRefreshTokenDateCreate,
                "Last Refresh Token Date Create can't be null."
        );
        this.dateCreate = requireNonNull(dateCreate, "Date create can't be null.");
    }
}
