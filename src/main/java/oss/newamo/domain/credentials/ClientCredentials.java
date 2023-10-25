package oss.newamo.domain.credentials;

import oss.backend.util.MappingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.Objects.requireNonNull;

public class ClientCredentials extends BaseCredentials {
    private final String accessToken;
    private final String refreshToken;

    public ClientCredentials(long spaceId, String clientId, String redirectUri,
            String amoCrmPath, String accessToken, String refreshToken) {
        super(spaceId, clientId, redirectUri, amoCrmPath);
        this.accessToken = requireNonNull(accessToken, "accessToken can't be null.");
        this.refreshToken = requireNonNull(refreshToken, "refreshToken can't be null.");
    }

    @JsonIgnore
    public String getAccessToken() {
        return accessToken;
    }

    @JsonIgnore
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
