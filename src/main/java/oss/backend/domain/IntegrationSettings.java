package oss.backend.domain;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public record IntegrationSettings(
        String id, String integrationName, String integrationDescription,
        @Nullable String iconPath, @Nullable String initialApiPath, @Nullable String detailsApiPath,
        boolean enabled
) {
    public IntegrationSettings(
            String id, String integrationName, String integrationDescription,
            @Nullable String iconPath, @Nullable String initialApiPath, @Nullable String detailsApiPath,
            boolean enabled
    ) {
        this.id = requireNonNull(id, "id can't be null.");
        this.integrationName = requireNonNull(integrationName, "integrationName can't be null.");
        this.integrationDescription = requireNonNull(integrationDescription, "integrationDescription can't be null.");
        this.iconPath = iconPath;
        this.initialApiPath = initialApiPath;
        this.detailsApiPath = detailsApiPath;
        this.enabled = enabled;
    }
}
