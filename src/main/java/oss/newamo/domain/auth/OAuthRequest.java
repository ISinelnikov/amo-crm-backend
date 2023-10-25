package oss.newamo.domain.auth;

import oss.backend.util.MappingUtils;

import java.util.Objects;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthRequest(String clientId, String clientSecret, String grandType,
                           @Nullable String code, @Nullable String refreshToken, String redirectUri) {
    public OAuthRequest(String clientId, String clientSecret, String grandType,
            @Nullable String code, @Nullable String refreshToken, String redirectUri) {
        this.clientId = Objects.requireNonNull(clientId, "clientId can't be null.");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret can't be null.");
        this.grandType = Objects.requireNonNull(grandType, "grandType can't be null.");
        this.code = code;
        this.refreshToken = refreshToken;
        this.redirectUri = Objects.requireNonNull(redirectUri, "redirectUri can't be null.");
    }

    @Override
    @JsonProperty("client_id")
    public String clientId() {
        return clientId;
    }

    @Override
    @JsonProperty("client_secret")
    public String clientSecret() {
        return clientSecret;
    }

    @Override
    @JsonProperty("grant_type")
    public String grandType() {
        return grandType;
    }

    @Nullable
    @Override
    @JsonProperty("code")
    public String code() {
        return code;
    }

    @Nullable
    @Override
    @JsonProperty("refresh_token")
    public String refreshToken() {
        return refreshToken;
    }

    @Override
    @JsonProperty("redirect_uri")
    public String redirectUri() {
        return redirectUri;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static OAuthRequest authorizationCode(String clientId, String clientSecret, String code, String redirectUri) {
        return new OAuthRequest(clientId, clientSecret, OAuthGrandType.AUTHORIZATION_CODE.getValue(),
                code, null, redirectUri);
    }

    public static OAuthRequest refreshToken(String clientId, String clientSecret, String refreshToken, String redirectUri) {
        return new OAuthRequest(clientId, clientSecret, OAuthGrandType.REFRESH_TOKEN.getValue(),
                null, refreshToken, redirectUri);
    }
}
