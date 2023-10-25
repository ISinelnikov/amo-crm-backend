package oss.newamo.domain.auth;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthResponse(String accessToken, String refreshToken
) {
    @JsonCreator
    public OAuthResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken
    ) {
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken can't be null.");
        this.refreshToken = Objects.requireNonNull(refreshToken, "refreshToken can't be null.");
    }
}
