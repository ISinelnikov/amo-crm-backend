package oss.newamo.domain.credentials;

import oss.backend.util.MappingUtils;

import static java.util.Objects.requireNonNull;

public class InitialCredentials extends BaseCredentials {
    private final String clientSecret;
    private final String code;

    public InitialCredentials(long spaceId, String clientId, String redirectUri,
            String amoCrmPath, String clientSecret, String code) {
        super(spaceId, clientId, redirectUri, amoCrmPath);
        this.clientSecret = requireNonNull(clientSecret, "clientSecret can't be null.");
        this.code = requireNonNull(code, "code can't be null.");
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }
}
