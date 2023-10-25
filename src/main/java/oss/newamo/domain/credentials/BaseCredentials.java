package oss.newamo.domain.credentials;

import oss.backend.util.RequestUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.Objects.requireNonNull;

public abstract class BaseCredentials {
    protected final long spaceId;
    protected final String clientId;
    protected final String redirectUri;
    protected final String amoCrmPath;

    protected BaseCredentials(long spaceId, String clientId, String redirectUri, String amoCrmPath) {
        this.spaceId = spaceId;
        this.clientId = requireNonNull(clientId, "clientId can't be null.");
        this.redirectUri = requireNonNull(redirectUri, "redirectUri can't be null.");
        this.amoCrmPath = requireNonNull(amoCrmPath, "amoCrmPath can't be null.")
                .startsWith(RequestUtils.HTTPS) ? amoCrmPath : RequestUtils.HTTPS + amoCrmPath;
    }

    public long getSpaceId() {
        return spaceId;
    }

    public String getClientId() {
        return clientId;
    }

    @JsonIgnore
    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAmoCrmPath() {
        return amoCrmPath;
    }
}
